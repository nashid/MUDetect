package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Ignore;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.DEFINITION;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Ignore
public class EncodeDefinitionsTest {
    @Test
    public void encodesOnlyDirectDefinitionEdges() throws Exception {
        AUG aug = buildAUG("void m(java.io.File f) {\n" +
                "  java.io.InputStream is = new java.io.FileInputStream(f);\n" +
                "  java.io.Reader r = new InputStreamReader(is);\n" +
                "  r.read();\n" +
                "}");

        assertThat(aug, hasEdge(actionNodeWithLabel("FileInputStream.<init>"), DEFINITION, dataNodeWithLabel("InputStream")));
        assertThat(aug, not(hasEdge(actionNodeWithLabel("FileInputStream.<init>"), DEFINITION, dataNodeWithLabel("Reader"))));
    }

    @Test
    public void encodesTransitiveDefinitionEdgesThroughArithmeticOperator() throws Exception {
        AUG aug = buildAUG("double m(java.util.Collection c) {\n" +
                "  double d = c.size() + 1f;\n" +
                "  return d;\n" +
                "}");

        assertThat(aug, not(hasEdge(actionNodeWithLabel("Collection.size()"), DEFINITION, dataNodeWithLabel("double"))));
    }
}