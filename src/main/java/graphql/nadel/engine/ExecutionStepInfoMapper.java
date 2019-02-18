package graphql.nadel.engine;

import graphql.Assert;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.language.Field;
import graphql.nadel.engine.transformation.FieldRenameTransformation;
import graphql.nadel.engine.transformation.FieldTransformation;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.util.FpKit;

import java.util.List;
import java.util.Map;

public class ExecutionStepInfoMapper {


    public ExecutionStepInfo mapExecutionStepInfo(ExecutionStepInfo executionStepInfo,
                                                  GraphQLSchema overallSchema,
                                                  Map<Field, FieldTransformation> transformationMap) {
        //TODO: handle __typename
        MergedField mergedField = executionStepInfo.getField();
        if (transformationMap.containsKey(mergedField.getSingleField())) {
            mergedField = unapplyTransformation(transformationMap.get(mergedField.getSingleField()), mergedField);
        }
        GraphQLOutputType fieldType = executionStepInfo.getType();
        GraphQLObjectType fieldContainer = executionStepInfo.getFieldContainer();
        GraphQLObjectType mappedFieldContainer = (GraphQLObjectType) overallSchema.getType(fieldContainer.getName());
        //TODO: the line below is not correct as it does not work list or non null types (since fieldType#getName will be null in that case)
        GraphQLOutputType mappedFieldType = (GraphQLOutputType) overallSchema.getType(fieldType.getName());
        GraphQLFieldDefinition fieldDefinition = executionStepInfo.getFieldDefinition();
        GraphQLFieldDefinition mappedFieldDefinition = mappedFieldContainer.getFieldDefinition(fieldDefinition.getName());

        // TODO: map path

        MergedField finalMergedField = mergedField;
        return executionStepInfo.transform(builder -> builder
                .field(finalMergedField)
                .type(mappedFieldType)
                .fieldContainer(mappedFieldContainer)
                .fieldDefinition(mappedFieldDefinition));

    }

    private MergedField unapplyTransformation(FieldTransformation fieldTransformation, MergedField mergedField) {
        if (fieldTransformation instanceof FieldRenameTransformation) {
            String originalName = ((FieldRenameTransformation) fieldTransformation).getOriginalName();
            List<Field> fields = FpKit.map(mergedField.getFields(), field -> field.transform(builder -> builder.name(originalName)));
            return MergedField.newMergedField(fields).build();
//        } else if (fieldTransformation instanceof HydrationTransformation) {
////            String originalName = ((HydrationTransformation) fieldTransformation).getOriginalName();
////            List<Field> fields = FpKit.map(mergedField.getFields(), field -> field.transform(builder -> builder.name(originalName)));
////            return MergedField.newMergedField(fields).build();
        }
        return Assert.assertShouldNeverHappen("unexpected transformation");
//        return mergedField;
    }
}