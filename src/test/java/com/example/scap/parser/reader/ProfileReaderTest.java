package com.example.scap.parser.reader;

import com.example.scap.model.parsed.xccdf.ParsedXccdfProfile;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileReaderTest {

    private static final XMLInputFactory2 FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final ProfileReader profileReader = new ProfileReader();

    @Test
    void readProfile_shouldParseIdTitleAndOnlySelectedTrueRuleIds()
            throws Exception {
        String xml = """
                <Profile id="profile-1">
                    <title>  Level 1 Server  </title>
                    <select idref="rule-1" selected="true"/>
                    <select idref="rule-2" selected="false"/>
                    <select idref="rule-3" selected="true"/>
                </Profile>
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        ParsedXccdfProfile profile = profileReader.readProfile(reader);

        assertEquals("profile-1", profile.getProfileId());
        assertEquals("Level 1 Server", profile.getTitle());
        assertEquals(List.of("rule-1", "rule-3"), profile.getSelectedRuleIds());
    }

    @Test
    void readProfile_shouldHandleMissingTitle()
            throws Exception {
        String xml = """
                <Profile id="profile-2">
                    <select idref="rule-10" selected="true"/>
                </Profile>
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        ParsedXccdfProfile profile = profileReader.readProfile(reader);

        assertEquals("profile-2", profile.getProfileId());
        assertNull(profile.getTitle());
        assertEquals(List.of("rule-10"), profile.getSelectedRuleIds());
    }

    @Test
    void readProfile_shouldHandleNoSelectElements()
            throws Exception {
        String xml = """
                <Profile id="profile-3">
                    <title>Empty Profile</title>
                </Profile>
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        ParsedXccdfProfile profile = profileReader.readProfile(reader);

        assertEquals("profile-3", profile.getProfileId());
        assertEquals("Empty Profile", profile.getTitle());
        assertTrue(profile.getSelectedRuleIds().isEmpty());
    }

    @Test
    void readProfile_shouldIgnoreUnrelatedElements()
            throws Exception {
        String xml = """
                <Profile id="profile-4">
                    <description>ignore me</description>
                    <platform idref="cpe:/o:vendor:product"/>
                    <title>Expected Title</title>
                    <select idref="rule-20" selected="true"/>
                </Profile>
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        ParsedXccdfProfile profile = profileReader.readProfile(reader);

        assertEquals("profile-4", profile.getProfileId());
        assertEquals("Expected Title", profile.getTitle());
        assertEquals(List.of("rule-20"), profile.getSelectedRuleIds());
    }

    @Test
    void readProfile_shouldUseFirstTitleWhenMultipleTitlesExist()
            throws Exception {
        String xml = """
                <Profile id="profile-5">
                    <title>First Title</title>
                    <title>Second Title</title>
                </Profile>
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        ParsedXccdfProfile profile = profileReader.readProfile(reader);

        assertEquals("profile-5", profile.getProfileId());
        assertEquals("First Title", profile.getTitle());
    }

    @Test
    void readProfile_shouldIgnoreSelectMissingIdref()
            throws Exception {
        String xml = """
                <Profile id="profile-6">
                    <title>Profile With Bad Select</title>
                    <select selected="true"/>
                </Profile>
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        ParsedXccdfProfile profile = profileReader.readProfile(reader);

        assertEquals("profile-6", profile.getProfileId());
        assertEquals("Profile With Bad Select", profile.getTitle());
        assertTrue(profile.getSelectedRuleIds().isEmpty());
    }

    @Test
    void readProfile_shouldIgnoreSelectWhenSelectedIsFalse()
            throws Exception {
        String xml = """
                <Profile id="profile-7">
                    <title>Profile</title>
                    <select idref="rule-1" selected="false"/>
                </Profile>
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        ParsedXccdfProfile profile = profileReader.readProfile(reader);

        assertEquals("profile-7", profile.getProfileId());
        assertTrue(profile.getSelectedRuleIds().isEmpty());
    }

    @Test
    void readProfile_shouldIgnoreSelectWhenSelectedMissing()
            throws Exception {
        String xml = """
                <Profile id="profile-8">
                    <title>Profile</title>
                    <select idref="rule-1"/>
                </Profile>
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        ParsedXccdfProfile profile = profileReader.readProfile(reader);

        assertEquals("profile-8", profile.getProfileId());
        assertTrue(profile.getSelectedRuleIds().isEmpty());
    }

    @Test
    void readProfile_shouldThrowWhenDocumentEndsBeforeProfileCloses()
            throws Exception {
        String xml = """
                <Profile id="profile-9">
                    <title>Broken Profile</title>
                    <select idref="rule-99" selected="true">
                """;

        XMLStreamReader2 reader = moveToProfileStart(xml);

        XMLStreamException ex = assertThrows(
                XMLStreamException.class,
                () -> profileReader.readProfile(reader)
        );

        assertNotNull(ex.getMessage());
    }

    private XMLStreamReader2 moveToProfileStart(String xml)
            throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLStreamReader2 reader = (XMLStreamReader2) FACTORY.createXMLStreamReader(in);

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT
                    && "Profile".equals(reader.getLocalName())) {
                return reader;
            }
        }

        throw new IllegalStateException("No Profile element found in test XML");
    }
}