package net.java.html.json;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className="ByteArray", properties = {
    @Property(name = "array", type = byte.class, array = true)
})
public class PrimitiveArrayTest {
    @Test public void generatedConstructorWithPrimitiveType() {
        byte[] arr = new byte[10];
        arr[3] = 10;
        ByteArray a = new ByteArray(arr);
        Assert.assertEquals(a.getArray().size(), 10, "Ten elements");
        Assert.assertEquals(a.getArray().get(3).byteValue(), 10, "Value ten");
    }
}
