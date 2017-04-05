package org.graylog2.plugins.slack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Strings.isNullOrEmpty;

public class SlackMessage {
    private final String channel;
    private final String userName;
    private final String message;
    private final String messageIcon;
    private final boolean linkNames;

    private final List<Attachment> attachments;

    public SlackMessage(String messageIcon, String message, String userName, String channel, boolean linkNames) {
        this.message = message;
        this.messageIcon = messageIcon;
        this.userName = userName;
        this.channel = channel;
        this.linkNames = linkNames;

        this.attachments = Lists.newArrayList();
    }

    public String getJsonString() {
        // See https://api.slack.com/methods/chat.postMessage for valid parameters
        final Map<String, Object> params = new HashMap<String, Object>(){{
            put("channel", channel);
            put("text", message);
            put("link_names", linkNames);
            put("parse", "none");
        }};

        if (!isNullOrEmpty(userName)) {
            params.put("username", userName);
        }

        if (!isNullOrEmpty(messageIcon)) {
            try {
                final URI uri = new URI(messageIcon);
                if (SlackPluginBase.isValidUriScheme(uri, "http", "https")) {
                    params.put("icon_url", messageIcon);
                }
                else {
                    params.put("icon_emoji", messageIcon);
                }
            } catch (URISyntaxException e) {
                params.put("icon_emoji", messageIcon);
            }
        }

        if (!attachments.isEmpty()) {
            params.put("attachments", attachments);
        }

        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not build payload JSON.", e);
        }
    }

    public Attachment addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        return attachment;
    }

    public Attachment addAttachment(String text, String color, String footerText, String footerIconUrl, Long ts) {
        Attachment attachment = new Attachment(text, null, color, footerText, footerIconUrl, ts, Lists.newArrayList());
        this.attachments.add(attachment);
        return attachment;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        @JsonProperty
        public String fallback;
        @JsonProperty
        public String text;
        @JsonProperty
        public String pretext;
        @JsonProperty
        public String color = "good";
        @JsonProperty("footer")
        public String footerText = "Graylog";
        @JsonProperty("footer_icon")
        public String footerIconUrl = "";
        @JsonProperty("ts")
        public Long ts;
        @JsonProperty
        public List<AttachmentField> fields;

        public Attachment(String text, String pretext, String color, String footerText, String footerIconUrl, Long ts, List<AttachmentField> fields) {
            this(text, text, pretext, color, footerText, footerIconUrl, ts, fields);
        }

        @JsonCreator
        public Attachment(String fallback, String text, String pretext, String color, String footerText, String footerIconUrl, Long ts, List<AttachmentField> fields) {
            this.fallback = fallback;
            this.text = text;
            this.pretext = pretext;
            this.color = color;
            this.footerText = footerText;
            this.footerIconUrl = footerIconUrl;
            this.ts = ts;
            this.fields = fields;
        }

        public Attachment addField(AttachmentField field) {
            this.fields.add(field);
            return this;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttachmentField {
        @JsonProperty
        public String title;
        @JsonProperty
        public String value;
        @JsonProperty("short")
        public boolean isShort = false;

        @JsonCreator
        public AttachmentField(String title, String value, boolean isShort) {
            this.title = title;
            this.value = value;
            this.isShort = isShort;
        }
    }

}
