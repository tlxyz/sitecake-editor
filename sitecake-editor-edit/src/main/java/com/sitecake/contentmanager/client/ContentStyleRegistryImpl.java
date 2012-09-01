package com.sitecake.contentmanager.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.sitecake.commons.client.util.JavaScriptRegExp;
import com.sitecake.commons.client.util.dom.CSSStyleRule;
import com.sitecake.commons.client.util.dom.StyleSheet;
import com.sitecake.contentmanager.client.container.ContentContainerFactory;

public class ContentStyleRegistryImpl implements ContentStyleRegistry {
	
	private Map<String, List<String>> styles;
	private ContentContainerFactory contentContainerFactory;
	
	@Inject
	public ContentStyleRegistryImpl(ContentContainerFactory contentContainerFactory) {
		this.contentContainerFactory = contentContainerFactory;
		styles = new HashMap<String, List<String>>();
		init();
	}
	
	@Override
	public List<String> get(String containerName, String itemSelector) {
		List<String> itemStyles = null;
		if ( styles.containsKey(containerName) ) {
			List<String> containerStyles = styles.get(containerName);
			itemStyles = new ArrayList<String>();
			for (String rawStyle : containerStyles) {
				if (rawStyle.startsWith(itemSelector) && rawStyle.length() > itemSelector.length()) {
					String itemStyle = rawStyle.substring(itemSelector.length()).replace('.', ' ').trim();
					if (!"".equals(itemStyle)) {
						itemStyles.add(itemStyle);
					}
				}
			}
		}
		return (itemStyles == null) ? null : (itemStyles.isEmpty() ? null : itemStyles);
	}

	private void init() {
		Map<String, Element> containers = contentContainerFactory.list();
		for (String containerName : containers.keySet()) {
			List<String> containerStyles = new ArrayList<String>();
			styles.put(containerName, containerStyles);
			Element element = containers.get(containerName);
			List<JavaScriptRegExp> matchers = new ArrayList<JavaScriptRegExp>();
			if (element.hasAttribute("id")) {
				matchers.add(JavaScriptRegExp.create("^\\s*#" + element.getAttribute("id") + "\\s+>?\\s*([^\\s,]+)\\s*$"));
			}
			String[] cssClasseNames = element.getClassName().trim().split("\\s+");
			for (String cssClassName : cssClasseNames) {
				matchers.add(JavaScriptRegExp.create("^\\s*." + cssClassName + "\\s+>?\\s*([^\\s,]+)\\s*$"));
			}
			
			JsArray<StyleSheet> styleSheets = StyleSheet.getAll();
			for (int i = 0; i < styleSheets.length(); i++) {
				StyleSheet styleSheet = styleSheets.get(i);
				if (styleSheet == null) continue;
				JsArray<CSSStyleRule> cssRules = styleSheet.cssRules();
				if (cssRules == null) continue;
				for (int j = 0; j < cssRules.length(); j++) {
					CSSStyleRule rule = cssRules.get(j);
					if (rule == null) continue;
					if (rule.selectorText() == null) continue;
					String[] selectors = rule.selectorText().split(",");
					for (String selector : selectors) {
						for (JavaScriptRegExp matcher : matchers) {
							JsArrayString matches = matcher.match(selector);
							if (matches != null && matches.length() == 2) {
								containerStyles.add(matches.get(1));
							}
						}
					}
				}
			}
			
		}
	}

}