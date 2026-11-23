package gg.jte.generated.ondemand;
import gg.jte.Content;
@SuppressWarnings("unchecked")
public final class JteaccountLayoutGenerated {
	public static final String JTE_NAME = "accountLayout.jte";
	public static final int[] JTE_LINE_INFO = {0,0,2,2,2,2,12,12,12,12,20,20,20,20,20,20,20,20,20,26,26,26,27,27,27,32,32,32,2,3,4,5,5,5,5};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String title, String csrfToken, Content account, Content detail) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(title);
		jteOutput.writeContent("</title>\n    <link rel=\"stylesheet\" href=\"/accounts.css\">\n</head>\n<body>\n    <nav aria-label=\"Bank navigation\">\n    <strong>Javalin Bank</strong>\n\n    <form method=\"post\" action=\"/logout\">\n        <input type=\"hidden\" name=\"csrfToken\"");
		var __jte_html_attribute_0 = csrfToken;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_0);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n        <button type=\"submit\">Log Out</button>\n    </form>\n</nav>\n\n    <main>\n        ");
		jteOutput.setContext("main", null);
		jteOutput.writeUserContent(account);
		jteOutput.writeContent("\n        ");
		jteOutput.setContext("main", null);
		jteOutput.writeUserContent(detail);
		jteOutput.writeContent("\n    </main>\n\n    <footer>© 2026 Javalin Bank</footer>\n</body>\n</html>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String title = (String)params.get("title");
		String csrfToken = (String)params.get("csrfToken");
		Content account = (Content)params.get("account");
		Content detail = (Content)params.get("detail");
		render(jteOutput, jteHtmlInterceptor, title, csrfToken, account, detail);
	}
}
