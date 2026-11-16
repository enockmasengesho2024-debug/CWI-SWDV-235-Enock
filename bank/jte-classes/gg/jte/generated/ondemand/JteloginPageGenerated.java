package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class JteloginPageGenerated {
	public static final String JTE_NAME = "loginPage.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,15,15,15,16,16,16,17,17,20,20,20,20,20,20,20,20,20,24,24,24,24,24,24,24,24,24,35,35,35,0,1,2,2,2,2};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String error, String username, String csrfToken) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>CWI Bank - Log In</title>\n</head>\n<body>\n    <h1>CWI Bank</h1>\n    <h2>Log In</h2>\n\n    ");
		if (!error.isEmpty()) {
			jteOutput.writeContent("\n        <p role=\"alert\">");
			jteOutput.setContext("p", null);
			jteOutput.writeUserContent(error);
			jteOutput.writeContent("</p>\n    ");
		}
		jteOutput.writeContent("\n\n    <form action=\"/login\" method=\"post\">\n        <input type=\"hidden\" name=\"csrfToken\"");
		var __jte_html_attribute_0 = csrfToken;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_0);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n        <p>\n            <label for=\"username\">Username</label>\n            <input id=\"username\" name=\"username\" type=\"text\"\n                  ");
		var __jte_html_attribute_1 = username;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_1);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(" autocomplete=\"username\" required>\n        </p>\n        <p>\n            <label for=\"password\">Password</label>\n            <input id=\"password\" name=\"password\" type=\"password\"\n                   maxlength=\"200\" autocomplete=\"current-password\" required>\n        </p>\n        <button type=\"submit\">Log In</button>\n    </form>\n</body>\n</html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String error = (String)params.get("error");
		String username = (String)params.get("username");
		String csrfToken = (String)params.get("csrfToken");
		render(jteOutput, jteHtmlInterceptor, error, username, csrfToken);
	}
}
