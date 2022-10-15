package dev.matheus.gladiador.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;

import java.util.List;

public class TextComponent_B {

	private final TextComponent textComponent;

	public TextComponent_B(final TextComponent textComponent) {
		this.textComponent = textComponent;
	}

	public TextComponent_B(final String message) {
		this.textComponent = new TextComponent(message);
	}

	public TextComponent_B(final String message, final ChatColor chatColor) {
		this.textComponent = new TextComponent(message);
		this.textComponent.setColor(chatColor);
	}

	public TextComponent_B setColor(final ChatColor chatColor) {
		if (this.textComponent != null)
			this.textComponent.setColor(chatColor);
		return this;
	}

	public TextComponent_B setBold(final boolean bold) {
		if (this.textComponent != null)
			this.textComponent.setBold(bold);
		return this;
	}

	public TextComponent_B setItalic(final boolean italic) {
		if (this.textComponent != null)
			this.textComponent.setItalic(italic);
		return this;
	}

	public TextComponent_B setObfuscated(final boolean obfuscated) {
		if (this.textComponent != null)
			this.textComponent.setObfuscated(obfuscated);
		return this;
	}

	public TextComponent_B setStrikethrough(final boolean strikethrough) {
		if (this.textComponent != null)
			this.textComponent.setStrikethrough(strikethrough);
		return this;
	}

	public TextComponent_B setUnderlined(final boolean underlined) {
		if (this.textComponent != null)
			this.textComponent.setUnderlined(underlined);
		return this;
	}

	public TextComponent_B setClickEvent(final ClickEvent clickEvent) {
		if (this.textComponent != null)
			this.textComponent.setClickEvent(clickEvent);
		return this;
	}

	public TextComponent_B setHoverEvent(final HoverEvent hoverEvent) {
		if (this.textComponent != null)
			this.textComponent.setHoverEvent(hoverEvent);
		return this;
	}

	public TextComponent_B setInsertion(final String insertion) {
		if (this.textComponent != null) {
			setInsertion(insertion);
		}
		return this;
	}

	public TextComponent_B setExtra(final List<BaseComponent> extra) {
		if (this.textComponent != null) {
			setExtra(extra);
		}
		return this;
	}

	public TextComponent_B setText(final String text) {
		if (this.textComponent != null)
			this.textComponent.setText(text);
		return this;
	}

	public TextComponent_B addExtra(final String text) {
		if (textComponent != null)
			textComponent.addExtra(text);
		return this;
	}

	public TextComponent_B addExtra(final BaseComponent component) {
		if (this.textComponent != null)
			this.textComponent.addExtra(component);
		return this;
	}

	public TextComponent_B copyFormatting(final BaseComponent component) {
		if (this.textComponent != null)
			copyFormatting(component);
		return this;
	}

	public TextComponent_B copyFormatting(final BaseComponent component, final boolean replace) {
		if (this.textComponent != null)
			copyFormatting(component, replace);
		return this;
	}

	public TextComponent_B copyFormatting(final BaseComponent component,
			final ComponentBuilder.FormatRetention retention, final boolean replace) {
		if (this.textComponent != null)
			copyFormatting(component, retention, replace);
		return this;
	}

	public TextComponent_B replace(final String regex, final String text) {
		if (this.textComponent != null)
			this.textComponent.setText(this.textComponent.getText().replace(regex, text));
		return this;
	}
	
	public TextComponent toTextComponent() {
		return this.textComponent;
	}
}