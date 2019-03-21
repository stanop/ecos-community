<@markup id="widgets">
    <@createWidgets group="dashlets"/>
</@>

<@markup id="html">
    <@uniqueIdDiv>
        <#assign id = args.htmlid?html />
        <#assign componentId = id + "-journals_component" />

        <div class="dashlet" style="border-radius: 12px">
            <div id="${componentId}"></div>
        </div>

        <script type="text/javascript">
            require([
                'js/citeck/ui/components/Journals/journalsDashlet/js/journalsDashlet.min',
                'xstyle!js/citeck/ui/components/Journals/journalsDashlet/css/journalsDashlet.min.css'
            ], function(JournalsDashlet) {
                JournalsDashlet.render('${componentId}', {id: 'dashletId-1-0-0'});
            });
        </script>
    </@>
</@>