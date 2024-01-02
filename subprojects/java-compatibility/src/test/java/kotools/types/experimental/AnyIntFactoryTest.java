package kotools.types.experimental;

import kotools.types.number.AnyInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class AnyIntFactoryTest {
    @Test
    public void create_should_pass_with_int() {
        final int expected = new Random()
                .nextInt();
        final AnyInt number = AnyIntFactory.create(expected);
        final int actual = number.toInt();
        Assertions.assertEquals(expected, actual);
    }
}
