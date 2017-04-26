package com.baidu.hugegraph.example;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.hugegraph.HugeFactory;
import com.baidu.hugegraph.HugeGraph;
import com.baidu.hugegraph.backend.query.ConditionQuery;
import com.baidu.hugegraph.schema.SchemaManager;
import com.baidu.hugegraph.type.HugeTypes;
import com.baidu.hugegraph.type.define.HugeKeys;

/**
 * Created by jishilei on 2017/4/2.
 */
public class Example2 {
    private static final Logger logger = LoggerFactory.getLogger(Example2.class);

    public static void main(String[] args) {

        logger.info("Example2 start!");
        String confFile = ExampleGraphFactory.class.getClassLoader().getResource("hugegraph.properties").getPath();
        HugeGraph graph = HugeFactory.open(confFile);
        graph.clearBackend();
        graph.initBackend();
        Example2.load(graph);
        //        traversal(graph);
        System.exit(0);
    }

    public static void traversal(final HugeGraph graph) {

        GraphTraversalSource g = graph.traversal();

        GraphTraversal<Vertex, Edge> edges = g.V().has("label", "software").has("name", "lop").outE();
        edges.toList().iterator().forEachRemaining(edge -> {
            System.out.println(edge.toString());
        });

    }

    public static void showSchema(final HugeGraph graph) {
        /************************* schemaManager operating *************************/
        SchemaManager schemaManager = graph.schema();

        logger.info("===============  show schema  ================");

        schemaManager.desc();
    }

    public static void load(final HugeGraph graph) {
        SchemaManager schema = graph.schema();

        schema.propertyKey("name").asText().create();
        schema.propertyKey("age").asInt().create();
        schema.propertyKey("lang").asText().create();
        schema.propertyKey("date").asText().create();

        schema.vertexLabel("person").properties("name", "age").primaryKeys("name").create();
        schema.vertexLabel("software").properties("name", "lang").primaryKeys("name").create();
        schema.vertexLabel("person").index("personByName").by("name").secondary().create();
        schema.vertexLabel("software").index("softwareByName").by("name").search().create();
        schema.vertexLabel("software").index("softwareByLang").by("lang").search().create();

        schema.edgeLabel("knows").properties("date").link("person", "person").create();
        schema.edgeLabel("created").properties("date").link("person", "software").create();
        schema.edgeLabel("created").index("createdByDate").by("date").secondary().create();

        graph.tx().open();
        Vertex marko = graph.addVertex(T.label, "person", "name", "marko", "age", 29);
        Vertex vadas = graph.addVertex(T.label, "person", "name", "vadas", "age", 27);
        Vertex lop = graph.addVertex(T.label, "software", "name", "lop", "lang", "java");
        Vertex josh = graph.addVertex(T.label, "person", "name", "josh", "age", 32);
        Vertex ripple = graph.addVertex(T.label, "software", "name", "ripple", "lang", "java");
        Vertex peter = graph.addVertex(T.label, "person", "name", "peter", "age", 35);

        marko.addEdge("knows", vadas, "date", "20160110");
        marko.addEdge("knows", josh, "date", "20130220");
        marko.addEdge("created", lop, "date", "20171210");
        josh.addEdge("created", ripple, "date", "20171210");
        josh.addEdge("created", lop, "date", "20091111");
        peter.addEdge("created", lop, "date", "20170324");

        graph.tx().commit();

        ConditionQuery q = new ConditionQuery(HugeTypes.SEARCH_INDEX);
        q.eq(HugeKeys.PROPERTY_VALUES, "java");
        q.eq(HugeKeys.INDEX_LABEL_NAME, "softwareByLang");
        System.out.print(graph.graphTransaction().query(q));
    }

}
