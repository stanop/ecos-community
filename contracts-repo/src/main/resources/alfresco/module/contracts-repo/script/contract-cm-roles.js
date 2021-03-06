<import resource="classpath:alfresco/module/contracts-repo/script/utils.js">

const ROLE_CONFIRMERS = "confirmers";
const ROLE_INITIATOR = "initiator";
const ROLE_SIGNER = "signer";
const ROLE_OWNER = "owner";
const ROLE_LAWYER = "lawyer";
const ROLE_ADDITIONAL_CONFIRMERS = "additional-confirmers";
const ROLE_AUDITOR = "auditor";

var roles = {
    rolesData: {},

    getConfirmersRefsFromRouting: function(type, kind) {

        var filterRoutings = function(routings) {
            var result = [];
            for (var i = 0; i < routings.length; i++) {
                var condition = routings[i].properties['route:scriptCondition'];
                if (condition != null && condition.replace(/\s/g, "").length != 0) {
                    var conditionResult;
                    try {
                        conditionResult = eval(condition);
                    } catch (e) {
                        logger.warn("Condition evaluation failed.");
                        logger.warn("MESSAGE: " + e);
                        logger.warn("CONDITION: " + condition);
                        conditionResult = false;
                    }
                    if (conditionResult) {
                        result.push(routings[i]);
                    }
                } else {
                    result.push(routings[i]);
                }
            }
            return result;
        };
        var getRouting = function(type, kind) {
            var query = 'TYPE:"route:route" AND @tk\\:appliesToType:"' + type.nodeRef + '"';

            if (kind) {
                query += ' AND @tk\\:appliesToKind:"' + kind.nodeRef + '"';
            } else {
                query += ' AND (ISNULL:"tk:appliesToKind" OR ISUNSET:"tk:appliesToKind")';
            }

            var routings = search.query({'query': query, 'language': 'fts-alfresco'}) || [];
            if (routings.length == 0 && kind) {
                routings = getRouting(type, null);
            }

            return filterRoutings(routings);
        };

        if (!type && !kind) return {};

        var confirmers = new Packages.java.util.ArrayList();

        var role = caseRoleService.getRole(document, ROLE_CONFIRMERS);
        var routing = (role.assocs['route:routeAssoc'] || [])[0];
        if (!routing) {
            routing = (getRouting(type, kind) || [])[0];
            if (routing) {
                role.createAssociation(routing, "route:routeAssoc");
            }
        }
        if (routing) {
            var precedence = routing.properties["route:precedence"];

            role.properties['route:precedence'] = precedence;
            role.save();

            var routeData = Packages.ru.citeck.ecos.workflow.confirm.PrecedenceToJsonListener.convertPrecedence(precedence);
            try {
                if (routeData && routeData.stages) {
                    var it = routeData.stages.iterator();
                    while (it.hasNext()) {
                        confirmers.addAll(it.next().confirmers);
                    }
                }
            } catch (e) {
                console.warn(e);
            }
        }

        var confirmersRefs = {};
        for (var i = 0; i < confirmers.size(); i++) {
            confirmersRefs[confirmers.get(i)['nodeRef']] = true;
        }
        return confirmersRefs;
    },

    getAssignees: function() {
        if (!role) return [];
        var roleName = role.properties['icaseRole:varName'];
        var data = roles.rolesData[roleName];
        return data ? data.fn() : [];
    }
};

(function() {
    roles.rolesData[ROLE_CONFIRMERS] = {
        fn: function () {
            var type = document.properties['tk:type'];
            var kind = document.properties['tk:kind'];

            var confirmersRefs = roles.getConfirmersRefsFromRouting(type, kind);


            var additionalConfirmers = caseRoleService.getAssignees(document, ROLE_ADDITIONAL_CONFIRMERS);
            for (var i in additionalConfirmers) {
                confirmersRefs[additionalConfirmers[i].nodeRef] = true;
            }

            var owner = (document.assocs['idocs:supervisor'] || [])[0];
            if (owner) {
                confirmersRefs[owner.nodeRef] = true;
            }

            var result = [];
            for (var ref in confirmersRefs) {
                result.push(search.findNode(ref));
            }
            return result;
        }
    };
    roles.rolesData[ROLE_INITIATOR] = {
        fn: function () {
            var initiator = (document.assocs['idocs:performer'] || [])[0];
            if (!initiator) {
                var creator = document.properties['cm:creator'];
                if (creator) {
                    initiator = people.getPerson(creator);
                }
            }
            return initiator ? [initiator] : [];
        }
    };
    roles.rolesData[ROLE_SIGNER] = {
        fn: function () {
            var signer = (document.assocs['idocs:signatory'] || [])[0];
            return signer ? [signer] : [];
        }
    };
    roles.rolesData[ROLE_OWNER] = {
        fn: function () {
            var owner = (document.assocs['idocs:supervisor'] || [])[0];
            return owner ? [owner] : [];
        }
    };
    roles.rolesData[ROLE_LAWYER] = {
        fn: function () {
            var lawyers = [];
            lawyers.push(people.getGroup("GROUP_law_department"));
            return lawyers;
        }
    };
    roles.rolesData[ROLE_AUDITOR] = {
        fn: function () {
            var auditors = [];
            auditors.push(people.getGroup("GROUP_auditor"));
            return auditors;
        }
    }
})();
