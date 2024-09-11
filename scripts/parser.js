const acorn = require("acorn");
const walk = require("acorn-walk");

// Sample AngularJS code (same as before)
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

// Parse the JavaScript code to get the AST
const ast = acorn.parse(jsCode, { sourceType: "module", ecmaVersion: 2020 });

// Object to store variable values
const variableValues = {};

// Function to check if a node is a makeRestCall invocation
function isMakeRestCall(node) {
    return (
        node.type === "CallExpression" &&
        node.callee.type === "Identifier" &&
        node.callee.name === "makeRestCall"
    );
}

// Create URL
function resolveNode(node) {
    if (node.type === "Literal") {
        // Return literal value
        return node.value;
    } else if (node.type === "Identifier") {
        // Check if the variable is known
        return variableValues[node.name] || '{?}';
    } else if (node.type === "BinaryExpression" && node.operator === "+") {
        // Resolve both sides of the binary expression (concatenation)
        const left = resolveNode(node.left);
        const right = resolveNode(node.right);
        return concatPaths(left, right);
    } else if (node.type === "CallExpression") {
        // Handle function calls as unknown
        return '{?}';
    } else {
        // Handle any other case as unknown
        return '{?}';
    }
}

// Helper function to concatenate paths, ensuring proper `/` separation
function concatPaths(left, right) {
    // Ensure that '/' is properly placed between the segments
    const leftStr = left.endsWith('/') ? left.slice(0, -1) : left;
    const rightStr = right.startsWith('/') ? right.slice(1) : right;
    return leftStr + '/' + rightStr;
}

// Walk through the AST and find variable declarations and makeRestCall calls
walk.simple(ast, {
    // Store values of found variables
    VariableDeclarator(node) {
        if (node.id.type === "Identifier" && node.init && node.init.type === "Literal") {
            variableValues[node.id.name] = node.init.value;
        }
    },
    // Process the makeRestCall invocations and resolve the URLs
    CallExpression(node) {
        if (isMakeRestCall(node)) {
            const urlArgument = node.arguments[0];
            const resolvedUrl = resolveNode(urlArgument);
            console.log(`Resolved REST call URL: ${resolvedUrl}`);

        }
    }
});
