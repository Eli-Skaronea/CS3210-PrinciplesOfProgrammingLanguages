import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MainTest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Main.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    @Test
    public void opMapHasAllCodes(){
        ArrayList<String> opcodes = new ArrayList<>();
        opcodes.add("MOVE");
        opcodes.add("MOVEI");
        opcodes.add("ADD");
        opcodes.add("INC");
        opcodes.add("SUB");
        opcodes.add("DEC");
        opcodes.add("MUL");
        opcodes.add("DIV");
        opcodes.add("BEQ");
        opcodes.add("BLT");
        opcodes.add("BGT");
        opcodes.add("BR");
        opcodes.add("END");

        boolean allIn = false;
        for(int i = 0; i < opcodes.size(); i++){
            if(!Main.opcodeMap.containsKey(opcodes.get(i))){
                allIn = false;
            }
        }
        assert allIn;
    }



}
