const babel = require("@babel/core");
const traverse = require("@babel/traverse").default;
const t = require("@babel/types");

const jsCode = `
'use strict';

/**
 * @ngdoc service
 * @name alertApp.dataService
 * @description
 * # dataService
 * Factory in the alertApp.
 */
angular.module('oauthApp')
    .factory('dataService', function ($http, $q) {
        var userApi = '/user-service/';
        var taskApi = '/task-service/';
        var loggedInUserApi = '/api/loggedinuser/me';

        var makeRestCall = function (url) {
            return $http.get(url)
                .then(function (response) {
                    if (typeof response.data === 'object') {
                        return response.data;
                    } else {
                        return $q.reject(response.data);
                    }
                }, function (response) {
                    return $q.reject(response.data);
                });
        };

        return {
            getAllUserData: function () {
                return makeRestCall(userApi);
            },
            getAllTaskData: function () {
                return makeRestCall(taskApi);
            },
            getUserDataByUserName: function (userName) {
                return makeRestCall(userApi + userName);
            },
            getTaskDataByTaskId: function (taskId) {
                return makeRestCall(taskApi + taskId);
            },
            getTaskDataByUserName: function (userName) {
                return makeRestCall(taskApi + 'usertask' + '/' + userName);
            },
            getLoggedInUser: function () {
                return makeRestCall(loggedInUserApi);
            }
        };
    });
`;

// Transform the code to get the AST
const output = babel.transform(jsCode, {
    ast: true,
    code: false,
    presets: ["@babel/preset-env"],
});

// Map to store variable values
const variableValues = {};

// Function to check if a call is an API call ($http.get or similar)
function isApiCall(node) {
    return (
        t.isMemberExpression(node.callee) &&
        t.isIdentifier(node.callee.object, { name: "$http" }) &&
        (t.isIdentifier(node.callee.property, { name: "get" }) ||
            t.isIdentifier(node.callee.property, { name: "post" }))
    );
}

function resolveValue(node) {
    if (t.isLiteral(node)) {
        // Return literal value
        return node.value;
    } else if (t.isIdentifier(node)) {
        // Check if the variable is known
        return variableValues[node.name] || '{?}';
    } else if (t.isBinaryExpression(node) && node.operator === "+") {
        // Resolve both sides of the binary expression (concatenation)
        const left = resolveValue(node.left);
        const right = resolveValue(node.right);
        return left + right;
    } else if (node.type === "CallExpression") {
        // Handle function calls as unknown
        return '{?}';
    } else {
        // Handle any other case as unknown
        return '{?}';
    }
}

// Traverse the AST
traverse(output.ast, {
    // Track variable declarations
    VariableDeclarator(path) {
        const varName = path.node.id.name;
        const init = path.node.init;
        if (init && (t.isLiteral(init) || t.isBinaryExpression(init))) {
            // Store the value of the variable (literals or binary expressions)
            variableValues[varName] = resolveValue(init);
        }
    },


    CallExpression(path) {
        if (isApiCall(path.node)) {
            const urlArg = path.node.arguments[0]; // First argument (URL)
            const resolvedUrl = resolveValue(urlArg); // Try to resolve the argument

            console.log(`Resolved URL: ${path.node.callee.object.name}.${path.node.callee.property.name} to ${resolvedUrl}`);
        }
    }

});