package bootiful.gateway;

import graphql.GraphQL;
import graphql.analysis.QueryVisitorFieldArgumentEnvironment;
import graphql.analysis.QueryVisitorFieldArgumentInputValue;
import graphql.execution.Execution;
import graphql.execution.nextgen.result.RootExecutionResultNode;
import graphql.language.*;
import graphql.parser.ParserOptions;
import graphql.schema.*;
import graphql.schema.validation.SchemaValidationErrorCollector;
import graphql.util.NodeAdapter;
import graphql.util.NodeZipper;
import org.reflections.Reflections;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.Configurable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.ResolvableType;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

record Customer(Integer id, String name) {
}

@Controller
class CustomerGraphqlController {

    @QueryMapping
    Collection<Customer> customers() {
        return List.of(new Customer(1, "Andreas"), new Customer(2, "Rossen"));
    }
}

@Configuration
@ImportRuntimeHints({
        GraphqlConfiguration.GraphqlControllerRuntimeHintsRegistrar.class})
class GraphqlConfiguration {

    /**
     * specific to my GraphQL application
     */
    static class GraphqlControllerRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            Set.of("graphql", "graphiql", "graph*ql/*.*").forEach(hints.resources()::registerPattern);
            Set.of(CustomerGraphqlController.class, Customer.class)
                    .forEach(c -> hints.reflection().registerType(c, MemberCategory.values()));
        }
    }

//    static class GraphqlRuntimeHintsRegistrar implements RuntimeHintsRegistrar {
//
//        @Override
//        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
//            var values = MemberCategory.values();
//            Set.of("graphql", "graphiql", "graph*ql/*.*").forEach(hints.resources()::registerPattern);
//            Set.of("i18n/Validation.properties", "i18n/Validation", "i18n/Execution.properties", "i18n/General.properties")
//                    .forEach(r -> hints.resources().registerResourceBundle(r));
//            Set.of("graphql.analysis.QueryTraversalContext", "graphql.schema.idl.SchemaParseOrder")
//                    .forEach(typeName -> hints.reflection().registerType(TypeReference.of(typeName), values));
//            Set.of(
//                            Argument.class, ArrayValue.class, Boolean.class, BooleanValue.class, DataFetchingEnvironment.class,
//                            Directive.class, DirectiveDefinition.class, DirectiveLocation.class, Document.class,
//                            EnumTypeDefinition.class, EnumTypeExtensionDefinition.class, EnumValue.class, EnumValueDefinition.class,
//                            Execution.class, Field.class, FieldDefinition.class, FloatValue.class, FragmentDefinition.class,
//                            FragmentSpread.class, GraphQL.class, GraphQLArgument.class, GraphQLCodeRegistry.Builder.class,
//                            GraphQLDirective.class, GraphQLEnumType.class, GraphQLEnumValueDefinition.class,
//                            GraphQLFieldDefinition.class, GraphQLInputObjectField.class, GraphQLInputObjectType.class,
//                            GraphQLInterfaceType.class, GraphQLList.class, GraphQLNamedType.class, GraphQLNonNull.class,
//                            GraphQLObjectType.class, GraphQLOutputType.class, GraphQLScalarType.class, GraphQLSchema.class,
//                            GraphQLSchemaElement.class, GraphQLUnionType.class, ImplementingTypeDefinition.class,
//                            InlineFragment.class, InputObjectTypeDefinition.class, InputObjectTypeExtensionDefinition.class,
//                            InputValueDefinition.class, IntValue.class, InterfaceTypeDefinition.class,
//                            InterfaceTypeExtensionDefinition.class, List.class, ListType.class, NodeAdapter.class, NodeZipper.class,
//                            NonNullType.class, NullValue.class, ObjectField.class, ObjectTypeDefinition.class,
//                            ObjectTypeExtensionDefinition.class, ObjectValue.class, OperationDefinition.class,
//                            OperationTypeDefinition.class, ParserOptions.class, QueryVisitorFieldArgumentEnvironment.class,
//                            QueryVisitorFieldArgumentInputValue.class, RootExecutionResultNode.class, ScalarTypeDefinition.class,
//                            ScalarTypeExtensionDefinition.class, SchemaDefinition.class, SchemaExtensionDefinition.class,
//                            SchemaValidationErrorCollector.class, SelectionSet.class, StringValue.class, TypeDefinition.class,
//                            TypeName.class, UnionTypeDefinition.class, UnionTypeExtensionDefinition.class, VariableDefinition.class,
//                            VariableReference.class
//                    ) //
//                    .forEach(aClass -> hints.reflection().registerType(aClass, values));
//        }
//    }


}

interface PersonClient {

    @GetExchange("/sample-json.json")
    Person get();
}

record Person(
        String firstName, String lastName, Address address) {
}

record Address(String streetAddress, String city, String state, String postalCode) {
}

@Configuration
class DeclarativeClientsConfiguration {

    @Bean
    ApplicationListener<ApplicationReadyEvent> readyEventApplicationListener(PersonClient personClient) {
        return event -> System.out.println(personClient.get());
    }

    @Bean
    PersonClient personClient(WebClient.Builder builder) {
        var adapter = WebClientAdapter.forClient(builder.baseUrl("https://tools.learningcontainer.com/").build());
        var proxyFactory = HttpServiceProxyFactory.builder(adapter).build();
        return proxyFactory.createClient(PersonClient.class);
    }
}


@Configuration
class GatewayConfiguration {


    @Bean
    RouteLocator gateway(RouteLocatorBuilder rlb) {
        return rlb
                .routes()
                .route(s -> s
                        .path("/proxy")
                        .filters(fs -> fs
                                .setPath("/sample-json.json")
                                .addResponseHeader("X-Josh-Loves-Gateway", "true")
                                .addResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        )
                        .uri("https://tools.learningcontainer.com/"))
                .build();
    }


    @Bean
    static BeanFactoryInitializationAotProcessor gatewayBeanFactoryInitializationAotProcessor() {
        return beanFactory -> (gc, beanFactoryInitializationCode) -> {
            var hints = gc.getRuntimeHints().reflection();
            var all = getConfigurableTypes(beanFactory);
            var mcs = MemberCategory.values();
            for (var c : all) {
                hints.registerType(c, mcs);
            }
        };
    }

    private static Set<Class<?>> getConfigurableTypes(BeanFactory beanFactory) {
        var classesToAdd = new HashSet<Class<? extends Configurable>>();
        var genericsToAdd = new HashSet<Class<?>>();

        for (var pkg : getAllPackages(beanFactory)) {
            var reflections = new Reflections(pkg);
            var subs = reflections.getSubTypesOf(Configurable.class);
            classesToAdd.addAll(subs);
        }

        for (var c : classesToAdd) {
            var rt = ResolvableType.forClass(c);
            if (rt.getSuperType().hasGenerics()) {
                var gens = rt.getSuperType().getGenerics();
                for (var g : gens) {
                    genericsToAdd.add(g.toClass());
                }
            }

        }

        var all = new HashSet<Class<?>>();
        all.addAll(classesToAdd);
        all.addAll(genericsToAdd);
        return all.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<String> getAllPackages(BeanFactory factory) {
        var packages = new HashSet<String>();
        packages.add("org.springframework.cloud.gateway");
        packages.addAll(AutoConfigurationPackages.get(factory));
        return packages;
    }
}
