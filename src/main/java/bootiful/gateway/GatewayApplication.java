package bootiful.gateway;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
        return java.util.List.of(new Customer(1, "Andreas"), new Customer(2, "Rossen"));
    }
}

@Configuration
@ImportRuntimeHints({
        GraphqlConfiguration.GraphqlRuntimeHintsRegistrar.class,
        GraphqlConfiguration.GraphqlControllerRuntimeHintsRegistrar.class})
class GraphqlConfiguration {

    /**
     * specific to my GraphQL application
     */
    static class GraphqlControllerRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            Set.of("graphql", "graphiql", "graph*ql/*.*").forEach(hints.resources()::registerPattern);
            Set.of(CustomerGraphqlController.class, Customer.class).forEach(c -> hints.reflection().registerType(c, MemberCategory.values()));
        }
    }

    static class GraphqlRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            var values = MemberCategory.values();
            Set.of("i18n/Validation.properties", "i18n/Validation", "i18n/Execution.properties", "i18n/General.properties")
                    .forEach(r -> hints.resources().registerResourceBundle(r));
            Set.of("graphql.analysis.QueryTraversalContext", "graphql.schema.idl.SchemaParseOrder")
                    .forEach(typeName -> hints.reflection().registerType(TypeReference.of(typeName), values));
            Set.of(
//                            graphql.GraphQL.class,
//                            graphql.analysis.QueryVisitorFieldArgumentEnvironment.class,
//                            graphql.analysis.QueryVisitorFieldArgumentInputValue.class,
//                            graphql.execution.Execution.class,
//                            graphql.execution.nextgen.result.RootExecutionResultNode.class,
//                            graphql.language.Argument.class,
//                            graphql.language.ArrayValue.class,
//                            graphql.language.BooleanValue.class,
//                            graphql.language.Directive.class,
//                            graphql.language.DirectiveDefinition.class,
//                            graphql.language.DirectiveLocation.class,
//                            graphql.language.Document.class,
//                            graphql.language.EnumTypeDefinition.class,
//                            graphql.language.EnumTypeExtensionDefinition.class,
//                            graphql.language.EnumValue.class,
//                            graphql.language.EnumValueDefinition.class,
//                            graphql.language.Field.class,
//                            graphql.language.FieldDefinition.class,
//                            graphql.language.FloatValue.class,
//                            graphql.language.FragmentDefinition.class,
//                            graphql.language.FragmentSpread.class,
//                            graphql.language.ImplementingTypeDefinition.class,
//                            graphql.language.InlineFragment.class,
//                            graphql.language.InputObjectTypeDefinition.class,
//                            graphql.language.InputObjectTypeExtensionDefinition.class,
//                            graphql.language.InputValueDefinition.class,
//                            graphql.language.IntValue.class,
//                            graphql.language.InterfaceTypeDefinition.class,
//                            graphql.language.InterfaceTypeExtensionDefinition.class,
//                            graphql.language.ListType.class,
//                            graphql.language.NonNullType.class,
//                            graphql.language.NullValue.class,
//                            graphql.language.ObjectField.class,
//                            graphql.language.ObjectTypeDefinition.class,
//                            graphql.language.ObjectTypeExtensionDefinition.class,
//                            graphql.language.ObjectValue.class,
//                            graphql.language.OperationDefinition.class,
//                            graphql.language.OperationTypeDefinition.class,
//                            graphql.language.ScalarTypeDefinition.class,
//                            graphql.language.ScalarTypeExtensionDefinition.class,
//                            graphql.language.SchemaDefinition.class,
//                            graphql.language.SchemaExtensionDefinition.class,
//                            graphql.language.SelectionSet.class,
//                            graphql.language.StringValue.class,
//                            graphql.language.TypeDefinition.class,
//                            graphql.language.TypeName.class,
//                            graphql.language.UnionTypeDefinition.class,
//                            graphql.language.UnionTypeExtensionDefinition.class,
//                            graphql.language.VariableDefinition.class,
//                            graphql.language.VariableReference.class ,
//                            graphql.parser.ParserOptions.class,
                            graphql.schema.DataFetchingEnvironment.class,
                            graphql.schema.GraphQLArgument.class,
//                            graphql.schema.GraphQLCodeRegistry.Builder.class,
                            graphql.schema.GraphQLDirective.class,
                            graphql.schema.GraphQLEnumType.class,
                            graphql.schema.GraphQLEnumValueDefinition.class,
                            graphql.schema.GraphQLFieldDefinition.class,
                            graphql.schema.GraphQLInputObjectField.class,
                            graphql.schema.GraphQLInputObjectType.class,
                            graphql.schema.GraphQLInterfaceType.class,
                            graphql.schema.GraphQLList.class,
                            graphql.schema.GraphQLNamedType.class,
                            graphql.schema.GraphQLNonNull.class,
                            graphql.schema.GraphQLObjectType.class,
                            graphql.schema.GraphQLOutputType.class,
                            graphql.schema.GraphQLScalarType.class,
                            graphql.schema.GraphQLSchema.class,
//                            graphql.schema.GraphQLSchemaElement.class,
                            graphql.schema.GraphQLUnionType.class
//                            graphql.schema.validation.SchemaValidationErrorCollector.class,
//                            graphql.util.NodeAdapter.class,
//                            graphql.util.NodeZipper.class,
//                            java.lang.Boolean.class,
//                            java.util.List.class
                    ) //
                    .forEach(aClass -> hints.reflection().registerType(aClass, values));
        }
    }


}
