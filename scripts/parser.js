const acorn = require("acorn");
const walk = require("acorn-walk");

// Sample Angular code with nested API calls
const angularCode = `
class ApiService {
  constructor(http) {
    this.http = http;
  }

  fetchData() {
    return this.http.get('https://api.example.com/data');
  }

  sendData(data) {
    return this.http.post('https://api.example.com/data', data);
  }
}
`;

// Parse the code to get the AST
const ast = acorn.parse(angularCode, { sourceType: "module", ecmaVersion: 2020 });

// List of HttpClient methods to identify
const httpClientMethods = ["get", "post", "put", "delete", "patch", "head", "options"];

// Function to check if a node represents an API call
function isHttpClientApiCall(node) {
    return (
        node.type === "CallExpression" &&
        node.callee.type === "MemberExpression" &&
        node.callee.object.type === "MemberExpression" &&
        node.callee.object.object.type === "ThisExpression" &&
        node.callee.object.property.name === "http" &&
        node.callee.property.type === "Identifier" &&
        httpClientMethods.includes(node.callee.property.name)
    );
}

// Walk through the AST and find API calls
walk.ancestor(ast, {
    CallExpression(node, ancestors) {
        if (isHttpClientApiCall(node)) {
            const functionName = ancestors.find((ancestor) => ancestor.type === "FunctionDeclaration" || ancestor.type === "MethodDefinition");
            const functionNameText = functionName ? functionName.key.name : "anonymous function";

            console.log(`Found API call: ${node.callee.property.name} in function ${functionNameText}`);
            console.log(`API URL: ${node.arguments[0].value}`);
        }
    }
});
