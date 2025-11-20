package com.graphhopper.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TranslationMapMockitoTest {

    private TranslationMap translationMap;

    @Mock
    private Translation englishTranslation;

    @Mock
    private Translation frenchTranslation;

    @BeforeEach
    void setUp() {
        translationMap = new TranslationMap();
    }

    //Cas 1: La traduction française est retournée quand la traduction est disponible.
    @Test
    void returnsRequestedLocale() {

        when(englishTranslation.getLocale()).thenReturn(Locale.ENGLISH);
        when(frenchTranslation.getLocale()).thenReturn(Locale.FRENCH);

        translationMap.add(englishTranslation);
        translationMap.add(frenchTranslation);

        Translation result = translationMap.getWithFallBack(Locale.FRENCH);

        assertSame(frenchTranslation, result, "La traduction française doit être utilisée quand elle est présente");
    }

    // Cas 2: Lorsqu'on a seulement la traduction anglaise, cela retourne automatiquement anglais.   
    @Test
    void fallsBackToEnglish() {

        when(englishTranslation.getLocale()).thenReturn(Locale.ENGLISH);
        translationMap.add(englishTranslation);

        Translation result = translationMap.getWithFallBack(Locale.FRENCH);

        assertSame(englishTranslation, result, "Si la traduction française n'existe pas, la traduction anglaise doit être utilisée.");
    }
}
