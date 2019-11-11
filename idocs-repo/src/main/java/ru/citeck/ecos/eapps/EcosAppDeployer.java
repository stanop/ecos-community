package ru.citeck.ecos.eapps;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.EcosAppsApiFactory;
import ru.citeck.ecos.apps.app.EcosApp;
import ru.citeck.ecos.apps.app.EcosAppMetaDto;
import ru.citeck.ecos.apps.app.EcosAppVersion;
import ru.citeck.ecos.apps.app.api.EcosAppDeployMsg;
import ru.citeck.ecos.apps.app.io.EcosAppIO;
import ru.citeck.ecos.utils.ResourceResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EcosAppDeployer implements ApplicationListener<ContextRefreshedEvent> {

    private RetryTemplate retryTemplate;

    private List<File> modules;
    private EcosAppIO ecosAppIO;
    private ModuleService moduleService;
    private EcosAppsApiFactory apiFactory;

    @Autowired
    public EcosAppDeployer(EcosAppsApiFactory apiFactory,
                           @Qualifier("resourceResolver") ResourceResolver resolver,
                           EcosAppIO ecosAppIO,
                           ModuleService moduleService) {

        this.apiFactory = apiFactory;
        this.ecosAppIO = ecosAppIO;
        this.moduleService = moduleService;
        try {
            Resource[] modulesResources = resolver.getResources("classpath*:alfresco/module/*");
            modules = Arrays.stream(modulesResources)
                    .filter(Resource::exists)
                    .map(m -> {
                        try {
                            return Optional.ofNullable(m.getFile());
                        } catch (FileNotFoundException e) {
                            // Resource is not a file. Skip it
                        } catch (Exception e) {
                            log.error("Error", e);
                        }
                        return Optional.<File>empty();
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            log.info("Found " + modules.size() + " module folders");

        } catch (Exception e) {
            log.error("Modules resolving error", e);
        }
    }

    public void resendPackages() {
        onApplicationEvent(null);
    }

    private void sendAppsImpl() {

        if (modules == null) {
            log.warn("Modules is not initialized");
            return;
        }

        for (File moduleDir : modules) {

            ModuleDetails module = moduleService.getModule(moduleDir.getName());
            if (module == null) {
                log.warn("Module is not registered: " + moduleDir.getName());
                continue;
            }

            EcosAppMetaDto meta = new EcosAppMetaDto();
            meta.setId(module.getId());
            meta.setName(module.getTitle());

            ModuleVersionNumber version = module.getModuleVersionNumber();
            if (version == null) {
                meta.setVersion(new EcosAppVersion("0"));
            } else {
                String versionStr = version.toString().replaceAll("[^0-9.]", "");
                meta.setVersion(new EcosAppVersion(versionStr.isEmpty() ? "0" : versionStr));
            }
            meta.setDependencies(Collections.emptyMap());

            try {
                sendApplication(ecosAppIO.read(moduleDir, meta));
            } catch (Exception e) {
                log.warn("Application parsing/sending error: " + module.getId(), e);
            }
        }
    }

    private boolean sendApplication(EcosApp app) {

        if (app.getModules().isEmpty()) {
            log.info("Application is empty " + app.getId() + " (" + app.getName() + ")");
            return true;
        }

        log.info("Deploying application: " + app.getId() + " (" + app.getName() + ")");

        RetryTemplate retryTemplate = getRetryTemplate();
        Object result = retryTemplate.execute(
                t -> {
                    apiFactory.getAppApi().deployApp("alfresco", app);
                    return null;
                },
                c -> {
                    log.error("Deploying application: " + app.getId() + " (" + app.getName() + ")");
                    return c.getLastThrowable();
                });

        return result == null;
    }

    private void sendApplications() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(this::sendAppsImpl);
        executor.shutdown();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        sendApplications();
    }

    private RetryTemplate getRetryTemplate() {

        if (retryTemplate == null) {
            retryTemplate = new RetryTemplate();

            FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
            fixedBackOffPolicy.setBackOffPeriod(10000);
            retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

            SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            retryPolicy.setMaxAttempts(6);
            retryTemplate.setRetryPolicy(retryPolicy);
        }

        return retryTemplate;
    }
}
