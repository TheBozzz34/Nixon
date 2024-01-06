package xyz.necrozma.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private int id;
    private String state;
    private String details;
    private String largeImageKey;
    private String largeImageText;
    public Message() {
    }
}
