package org.bedwars.stats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un titolo che viene visualizzato prima di ogni messaggio di un giocatore nella chat.
 */
public class ChatFlair {
    private static final List<ChatFlair> flairs = new ArrayList<>();
    private static int NEXT_ID = 0;
    private final TextComponent chatView;
    private final TextComponent description;
    private final int id;

    /**
     * Crea una {@code ChatFlair}.
     * @param chatView la rappresentazione nella chat
     */
    public ChatFlair(TextComponent chatView) {
        this(chatView, Component.empty());
    }

    /**
     * Crea una {@code ChatFlair}.
     * @param chatView la rappresentazione nella chat
     * @param description la descrizione
     */
    public ChatFlair(TextComponent chatView, TextComponent description) {
        this.chatView = chatView;
        this.description = description;
        this.id = NEXT_ID++;
        flairs.add(this);
    }

    /**
     * Restituisce la rappresentazione nella chat se è una flair completa, altrimenti restituisce un {@code TextComponent} vuoto.
     * @return la rappresentazione nella chat se è una flair completa, altrimenti {@code Component.empty()}
     */
    public TextComponent getChatView() {
        return (chatView.content().equals("Nessun flair") ? Component.empty() : chatView.append(Component.text(" "))).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Restituisce la rappresentazione nella chat.
     * @return la rappresentazione nella chat
     */
    public TextComponent getGUIView() {
        return chatView;
    }

    /**
     * Restituisce la descrizione.
     * @return la descrizione
     */
    public TextComponent getDescription() {
        return description.decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
    }

    /**
     * Restituisce l'ID.
     * @return l'ID
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce la {@code ChatFlair} con l'ID dato.
     * @param id l'ID da controllare
     * @return la {@code ChatFlair} trovata
     */
    public static ChatFlair getFlair(int id) {
        return flairs.get(id);
    }

    /**
     * Restituisce il numero di {@code ChatFlair}.
     * @return il numero di {@code ChatFlair}
     */
    public static int getFlairCount() {
        return flairs.size();
    }
}
