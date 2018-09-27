import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, compose } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'js/citeck/lib/redux-thunk';

import ShareHeader from './share-header';
import API from './misc/api';
import rootReducer from './reducers';
import {
    setUserName,
    setUserFullName,
    setUserNodeRef,
    setUserIsAdmin,
    setUserIsAvailable,
    setCurrentSiteId,
    loadTopMenuData,
    loadUserMenuPhoto
} from './actions';

import "xstyle!js/citeck/lib/css/bootstrap.min.css";

// TODO include polyfills

const api = new API(window.Alfresco.constants.PROXY_URI);

let composeEnhancers = compose;
if (typeof window === 'object' && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__) {
    composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__;
}

const store = createStore(rootReducer, {}, composeEnhancers(
        applyMiddleware(thunk.withExtraArgument(api)),
    )
);

export const render = (elementId, props) => {
    store.dispatch(setUserName(props.userName));
    store.dispatch(setUserFullName(props.userFullname));
    store.dispatch(setUserNodeRef(props.userNodeRef));
    store.dispatch(setUserIsAdmin(props.userIsAdmin === "true"));
    store.dispatch(setUserIsAvailable(props.userIsAvailable === "true"));
    store.dispatch(loadUserMenuPhoto(props.userNodeRef));

    store.dispatch(setCurrentSiteId(props.site));

    store.dispatch(loadTopMenuData(props.site, props.userName, props.userIsAvailable === "true"));

    ReactDOM.render(
        <Provider store={store}>
            <ShareHeader { ...props } />
        </Provider>,
        document.getElementById(elementId)
    );
};
