import React from "react";
import NodeCardlet from '../node-cardlet';

import 'xstyle!./tasks-manual.css';

export default class TasksManual extends NodeCardlet {

    static getFetchUrl(ownProps) {
        return '/share/proxy/alfresco/citeck/document/tasks-manual?nodeRef=' + ownProps.nodeRef;
    }

    static fetchData(ownProps, onSuccess, onFailure) {
        let htmlId = 'tasks-manual-cardlet_' + ownProps.id;
        let headerId = ownProps.controlProps.header || '';

        NodeCardlet.fetchData.call(
            this,
            ownProps,
            (data) => {
                onSuccess({
                    ...data,
                    htmlId: htmlId,
                    header: Alfresco.util.message(headerId),
                });
            },
            onFailure
        );
    }

    componentDidMount() {
        let htmlId = this.props.data.htmlId;
        Alfresco.util.createTwister(`${htmlId}-heading`, 'dc');
    }

    render() {
        let props = this.props;
        let data = props.data;
        let isLoading = props.isFetching || data.nodePendingUpdate;
        let loadingClass = isLoading !== false ? 'loading' : '';
        let tasks = data.tasks;

        const htmlId = data.htmlId;
        const header = data.header;

        if(!tasks.length){
            return (null);
        }

        return (
            <div id={`${htmlId}-panel`} className="document-children document-details-panel">
                <h2 id={`${htmlId}-heading`} className="thin dark">
                    {header}
                    <span id={`${htmlId}-heading-actions`} className="alfresco-twister-actions" style={{position: 'relative', float: 'right'}}/>
                </h2>

                <div className="panel-body simple-doclist">
                    <div className={loadingClass}>
                        {tasks.map(t => {
                            return (
                                <div id={`${htmlId}-${t.id}`} className="yui-dt-liner">
                                    <h3 className="thin dark" className="filename simple-view">{t.title}</h3>
                                    <div dangerouslySetInnerHTML={{__html: t.description}} className="detail"/>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>
        );
    }
}
