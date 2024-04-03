package lab1.app;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Singular;

@Builder(setterPrefix = "with", toBuilder = true)
@Getter
public class ObjectFormatter<T> {
    @Default
    private AttributedStyle fieldStyle = new AttributedStyle();
    @Default
    private AttributedStyle valueStyle = new AttributedStyle();
    @Default
    private AttributedString fieldSeparator = new AttributedString("\n");
    @Default
    private AttributedString fieldValueSeparator = new AttributedString(": ");
    @Default
    private AttributedString prefix = new AttributedString("");
    @Default
    private AttributedString suffix = new AttributedString("");
    @Singular
    private Map<String, Function<T, ?>> fieldSelectors;

    public AttributedString format(T object) {
        var builder = new AttributedStringBuilder();
        builder.append(prefix);
        fieldSelectors.forEach((name, selector) -> builder
                .styled(fieldStyle, name)
                .append(fieldValueSeparator)
                .styled(valueStyle, selector.apply(object).toString())
                .append(fieldSeparator));
        return builder.toAttributedString();
    }

    private <U> ObjectFormatterBuilder<U> forType() {
        return (ObjectFormatterBuilder<U>) new ObjectFormatter<U>(fieldStyle, valueStyle, fieldSeparator, fieldValueSeparator, prefix, suffix,
                Collections.emptyMap()).toBuilder();
    }

    public static class ObjectFormatterBuilder<T> {
        public <U> ObjectFormatterBuilder<U> forType() {
            return build().<U>forType();
        }
    }
}
