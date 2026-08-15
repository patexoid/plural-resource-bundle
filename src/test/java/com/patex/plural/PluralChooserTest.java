package com.patex.plural;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

public class PluralChooserTest {

    private PluralChooserFactory factory;

    @Before
    public void setUp() {
        factory = new PluralChooserFactory();
    }

    private PluralMessageFormat formatterFor(Locale locale, String... wordForms) {
        PluralChooser chooser = factory.getFormChooser(locale);
        if (wordForms.length > 0) {
            chooser.putWord(wordForms);
        }
        return new PluralMessageFormat(chooser);
    }

    @Test
    public void shouldFormatNumberSubformatUsingBundleLocaleNotJvmDefault() {
        // {0,number} must use the chooser's own locale (Germany: '.' as the
        // thousands separator), regardless of the JVM's default locale.
        PluralMessageFormat germanFormat = formatterFor(Locale.GERMANY);
        Assert.assertEquals("1.234", germanFormat.format("{0,number}", 1234));

        PluralMessageFormat usFormat = formatterFor(Locale.US);
        Assert.assertEquals("1,234", usFormat.format("{0,number}", 1234));
    }

    @Test
    public void shouldFormatSimplePlural() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "book", "books");
        Assert.assertEquals("1 book", messageFormat.format("{0} <p:book>", 1));
        Assert.assertEquals("2 books", messageFormat.format("{0} <p:book>", 2));
    }

    @Test
    public void shouldLeaveWordUnchangedWhenNotRegistered() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH);
        Assert.assertEquals("1 book", messageFormat.format("{0} <p:book>", 1));
        Assert.assertEquals("2 book", messageFormat.format("{0} <p:book>", 2));
    }

    @Test
    public void shouldTreatUnterminatedPluralMarkersAsLiteralText() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH);
        Assert.assertEquals("1 <", messageFormat.format("{0} <", 1));
        Assert.assertEquals("1 <p", messageFormat.format("{0} <p", 1));
        Assert.assertEquals("1 <p:", messageFormat.format("{0} <p:", 1));
        Assert.assertEquals("1 ", messageFormat.format("{0} <p:>", 1));
    }

    @Test
    public void shouldPreserveCapitalization() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "book", "books");
        Assert.assertEquals("1 Book", messageFormat.format("{0} <p:Book>", 1));
        Assert.assertEquals("2 Books", messageFormat.format("{0} <p:Book>", 2));
    }

    @Test
    public void shouldPreserveCapitalizationForSingleCharWord() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "b", "bs");
        Assert.assertEquals("1 B", messageFormat.format("{0} <p:B>", 1));
        Assert.assertEquals("2 Bs", messageFormat.format("{0} <p:B>", 2));
    }

    @Test
    public void shouldUseExplicitArgumentIndex() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "book", "books");
        Assert.assertEquals("1 books", messageFormat.format("{0} <p:{1}book>", 1, 2));
        Assert.assertEquals("2 book", messageFormat.format("{0} <p:{1}book>", 2, 1));
    }

    @Test
    public void shouldFormatMultiplePluralsInOnePattern() {
        PluralChooser chooser = factory.getFormChooser(Locale.ENGLISH);
        chooser.putWord("book", "books");
        chooser.putWord("sequence", "sequences");
        PluralMessageFormat messageFormat = new PluralMessageFormat(chooser);
        Assert.assertEquals("10 books 2 sequences", messageFormat.format("{0} <p:book> {1} <p:sequence>", 10, 2));
    }

    @Test
    public void shouldFormatRussianThreeFormPlurals() {
        PluralChooser chooser = factory.getFormChooser(new Locale("ru"));
        chooser.putWord("книга", "книги", "книг");
        PluralMessageFormat messageFormat = new PluralMessageFormat(chooser);

        Assert.assertEquals("1 книга", messageFormat.format("{0} <p:книга>", 1));
        Assert.assertEquals("21 книга", messageFormat.format("{0} <p:книга>", 21));

        Assert.assertEquals("2 книги", messageFormat.format("{0} <p:книга>", 2));
        Assert.assertEquals("24 книги", messageFormat.format("{0} <p:книга>", 24));

        Assert.assertEquals("5 книг", messageFormat.format("{0} <p:книга>", 5));
        Assert.assertEquals("11 книг", messageFormat.format("{0} <p:книга>", 11));
        Assert.assertEquals("36 книг", messageFormat.format("{0} <p:книга>", 36));
    }

    // The following tests document malformed-input handling in
    // PluralMessageFormat#format(char[], Object...). Bad input that a
    // caller could easily produce (e.g. a typo in a properties file) fails
    // with a PluralFormatException whose message names the pattern, the
    // failing position and the reason, instead of a bare
    // ArrayIndexOutOfBoundsException/ClassCastException/NumberFormatException.

    @Test
    public void shouldThrowWhenPluralTagUnclosed() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "book", "books");
        PluralFormatException e = Assert.assertThrows(PluralFormatException.class,
                () -> messageFormat.format("{0} <p:book", 1));
        Assert.assertTrue(e.getMessage().contains("unclosed"));
        Assert.assertTrue(e.getMessage().contains("position 4"));
    }

    @Test
    public void shouldThrowWhenPluralTagHasNoPrecedingIndex() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "book", "books");
        PluralFormatException e = Assert.assertThrows(PluralFormatException.class,
                () -> messageFormat.format("<p:book>", 1));
        Assert.assertTrue(e.getMessage().contains("book"));
        Assert.assertTrue(e.getMessage().contains("no preceding"));
    }

    @Test
    public void shouldThrowWhenArgumentBraceUnclosed() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH);
        PluralFormatException e = Assert.assertThrows(PluralFormatException.class,
                () -> messageFormat.format("{0", 1));
        Assert.assertTrue(e.getMessage().contains("unclosed"));
    }

    @Test
    public void shouldThrowWhenPluralArgumentIsNotAnInteger() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "book", "books");
        PluralFormatException e = Assert.assertThrows(PluralFormatException.class,
                () -> messageFormat.format("{0} <p:book>", "not-a-number"));
        Assert.assertTrue(e.getMessage().contains("book"));
        Assert.assertTrue(e.getMessage().contains("String"));
    }

    @Test
    public void shouldThrowWhenArgumentIndexIsNotNumeric() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "book", "books");
        PluralFormatException e = Assert.assertThrows(PluralFormatException.class,
                () -> messageFormat.format("{0} <p:{x}book>", 1, 2));
        Assert.assertTrue(e.getMessage().contains("numeric"));
        Assert.assertTrue(e.getMessage().contains("x"));
    }

    @Test
    public void shouldThrowWhenPluralArgumentIndexOutOfRange() {
        PluralMessageFormat messageFormat = formatterFor(Locale.ENGLISH, "book", "books");
        PluralFormatException e = Assert.assertThrows(PluralFormatException.class,
                () -> messageFormat.format("{0} <p:{5}book>", 1));
        Assert.assertTrue(e.getMessage().contains("book"));
        Assert.assertTrue(e.getMessage().contains("index 5"));
        Assert.assertTrue(e.getMessage().contains("only 1 argument"));
    }
}