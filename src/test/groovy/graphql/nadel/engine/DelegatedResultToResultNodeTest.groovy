package graphql.nadel.engine


import graphql.execution.nextgen.result.ResultNodesUtil
import graphql.nadel.DelegatedExecutionResult
import graphql.nadel.TestUtil
import spock.lang.Ignore
import spock.lang.Specification

class DelegatedResultToResultNodeTest extends Specification {


    def "simple query"() {

        def data = ["hello": "world"]
        def schema = TestUtil.schema("type Query{ hello: String }")
        def query = TestUtil.parseQuery("{hello}")

        def (executionContext, fieldSubSelection) = TestUtil.executionData(schema, query)

        DelegatedResultToResultNode resultToNodes = new DelegatedResultToResultNode()
        DelegatedExecutionResult delegatedResult = new DelegatedExecutionResult(data)

        when:
        def node = resultToNodes.resultToResultNode(executionContext, delegatedResult, fieldSubSelection)
        def executionResult = ResultNodesUtil.toExecutionResult(node)

        then:
        executionResult.data == data
    }


    def "query with alias "() {

        def data = ["myAlias": "world"]
        def schema = TestUtil.schema("type Query{ hello: String }")
        def query = TestUtil.parseQuery("{myAlias: hello}")

        def (executionContext, fieldSubSelection) = TestUtil.executionData(schema, query)

        DelegatedResultToResultNode resultToNodes = new DelegatedResultToResultNode()
        DelegatedExecutionResult delegatedResult = new DelegatedExecutionResult(data)

        when:
        def node = resultToNodes.resultToResultNode(executionContext, delegatedResult, fieldSubSelection)
        def executionResult = ResultNodesUtil.toExecutionResult(node)

        then:
        executionResult.data == data
    }

    def "bigger query"() {

        def data = ["foo": ["bar": ["name": "myName", "id": "myId"]], "foo2": ["bar": ["name": "myName2", "id": "myId2"]]]
        def schema = TestUtil.schema("""
        type Query{ 
            foo: Foo
            foo2: Foo
        }
        type Foo {
            bar: Bar
        }
        type Bar {
            id: ID
            name: String
        }
        
        """)
        def query = TestUtil.parseQuery("""
        {foo {
            bar{
                name
                id
            }
        }
        foo2{
            bar{
                name
                id
            }
        }}
        """)
        def (executionContext, fieldSubSelection) = TestUtil.executionData(schema, query)

        DelegatedResultToResultNode resultToNodes = new DelegatedResultToResultNode()
        DelegatedExecutionResult delegatedResult = new DelegatedExecutionResult(data)

        when:
        def node = resultToNodes.resultToResultNode(executionContext, delegatedResult, fieldSubSelection)
        def executionResult = ResultNodesUtil.toExecutionResult(node)

        then:
        executionResult.data == data
    }


    def "unknown field"() {
        def data = ["unknown field": "world"]
        def schema = TestUtil.schema("type Query{ hello: String }")
        def query = TestUtil.parseQuery("{hello}")

        def (executionContext, fieldSubSelection) = TestUtil.executionData(schema, query)

        DelegatedResultToResultNode resultToNodes = new DelegatedResultToResultNode()
        DelegatedExecutionResult delegatedResult = new DelegatedExecutionResult(data)

        when:
        def node = resultToNodes.resultToResultNode(executionContext, delegatedResult, fieldSubSelection)
        def executionResult = ResultNodesUtil.toExecutionResult(node)

        then:
        executionResult.data == ["hello": null]
    }

    def "invalid return type: List instead Int"() {
        def data = ["hello": ["world1", "world2"]]
        def schema = TestUtil.schema("type Query{ hello: Int }")
        def query = TestUtil.parseQuery("{hello}")

        def (executionContext, fieldSubSelection) = TestUtil.executionData(schema, query)

        DelegatedResultToResultNode resultToNodes = new DelegatedResultToResultNode()
        DelegatedExecutionResult delegatedResult = new DelegatedExecutionResult(data)

        when:
        def node = resultToNodes.resultToResultNode(executionContext, delegatedResult, fieldSubSelection)
        def executionResult = ResultNodesUtil.toExecutionResult(node)

        then:
        executionResult.data == ["hello": null]
        executionResult.errors.size() == 1
    }

    // Fails currently because the String scalar coerces everything to String
    @Ignore
    def "invalid return type: List instead String"() {
        def data = ["hello": ["world1", "world2"]]
        def schema = TestUtil.schema("type Query{ hello: String }")
        def query = TestUtil.parseQuery("{hello}")

        def (executionContext, fieldSubSelection) = TestUtil.executionData(schema, query)

        DelegatedResultToResultNode resultToNodes = new DelegatedResultToResultNode()
        DelegatedExecutionResult delegatedResult = new DelegatedExecutionResult(data)

        when:
        def node = resultToNodes.resultToResultNode(executionContext, delegatedResult, fieldSubSelection)
        def executionResult = ResultNodesUtil.toExecutionResult(node)

        then:
        executionResult.data == ["hello": null]
        executionResult.errors.size() == 1

    }


}
