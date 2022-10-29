package bootiful.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.HashSet;


class GatewayApplicationTests {

    @Test
    void json() throws Exception {
        var graphqlNodes = new HashSet<String>();
        try (var in = new ClassPathResource(
                "META-INF/native-image/reflect-config.json")
                .getInputStream()) {
            var json = new ObjectMapper();
            var root = json.readValue(in, JsonNode.class);
            root.forEach(jn -> {
                if (jn.has("name") && jn.get("name").textValue().startsWith("graphql")) {
                    graphqlNodes.add(jn.get("name").textValue());
                }
            });
        }

        var newJson = """
                {
                    "name": "%s",
                    "queryAllDeclaredConstructors": true,
                    "queryAllPublicMethods": true,
                    "allPublicFields": true,
                    "allDeclaredMethods": true,
                    "allPublicMethods": true,
                    "allPublicConstructors": true,
                    "queryAllDeclaredMethods": true,
                    "queryAllPublicConstructors": true,
                    "allDeclaredClasses": true,
                    "allDeclaredFields": true,
                    "allPublicClasses": true,
                    "allDeclaredConstructors": true
                  },
                """;


        var allTogether = "[" + String.join("  ",
                graphqlNodes
                        .stream()
                        .map(cn -> String.format(newJson, cn))
                        .toList()
        ) + "]";
        System.out.println(allTogether);
    }

}
