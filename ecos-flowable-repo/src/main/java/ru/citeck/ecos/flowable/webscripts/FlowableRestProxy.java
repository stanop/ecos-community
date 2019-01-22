package ru.citeck.ecos.flowable.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.content.MimetypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.webscripts.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import ru.citeck.ecos.flowable.services.rest.FlowableRestTemplate;

import java.io.IOException;

public class FlowableRestProxy extends AbstractWebScript {

    private static final String WEBSCRIPT_URL = "citeck/flowable/proxy/";
    private static final String FLOWABLE_HOST_KEY = "${flowable.host.url}";

    @Value(FLOWABLE_HOST_KEY)
    private String flowableHost;

    @Autowired
    private FlowableRestTemplate restTemplate;
    @Autowired
    @Qualifier("sysAdminParams")
    private SysAdminParams sysAdminParams;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        if (flowableHost == null || flowableHost.isEmpty() || FLOWABLE_HOST_KEY.equals(flowableHost)) {
            flowableHost = sysAdminParams.getAlfrescoProtocol() + "://" +
                           sysAdminParams.getAlfrescoHost() + ":" +
                           sysAdminParams.getAlfrescoPort();
        }

        HttpMethod method;
        if (getDescription().getId().endsWith(".post")) {
            method = HttpMethod.POST;
        } else {
            method = HttpMethod.GET;
        }

        HttpEntity<Object> requestEntity = null;
        Object body = getBody(req);
        if (body != null) {
            requestEntity = new HttpEntity<>(body);
        }

        String url = getFlowableUrl(req.getURL());
        ResponseEntity<String> entity = restTemplate.exchange(url, method, requestEntity, String.class);

        entity.getHeaders().forEach((key, value) -> {
            if ("Content-Type".equals(key) && value.size() > 0) {
                res.setContentType(value.get(0));
            }
        });

        res.setContentEncoding("UTF-8");
        res.getWriter().write(entity.getBody());
        res.setStatus(entity.getStatusCode().value());
    }

    private Object getBody(WebScriptRequest req) throws IOException {

        String contentType = req.getContentType();
        if (contentType == null) {
            contentType = "";
        }

        String bodyStr = req.getContent().getContent();
        if (bodyStr == null || bodyStr.length() == 0) {
            return bodyStr;
        }

        Object body;
        if (contentType.contains(MimetypeMap.MIMETYPE_JSON)) {
            body = mapper.readTree(bodyStr);
        } else {
            body = bodyStr;
        }

        return body;
    }

    private String getFlowableUrl(String baseUrl) {

        String url = baseUrl;

        int flbUrlIndex = url.indexOf(WEBSCRIPT_URL) + WEBSCRIPT_URL.length() - 1;
        url = flowableHost + url.substring(flbUrlIndex);
        url = url.replaceAll("alf_ticket=TICKET_[^&]+&?", "");

        char lastChar = url.charAt(url.length() - 1);
        if (lastChar == '?' || lastChar == '&') {
            url = url.substring(0, url.length() - 1);
        }

        if (!url.contains("?") && url.indexOf(".", url.lastIndexOf("/")) == -1) {
            url = url + "/";
        }

        return url;
    }
}