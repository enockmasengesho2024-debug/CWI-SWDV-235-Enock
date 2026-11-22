package gg.jte.generated.ondemand;
import java.util.Map;
@SuppressWarnings("unchecked")
public final class JtetransactionsListGenerated {
	public static final String JTE_NAME = "transactionsList.jte";
	public static final int[] JTE_LINE_INFO = {0,0,2,2,2,2,7,7,7,9,9,11,11,13,13,13,14,14,14,15,15,15,15,17,17,17,17,18,18,18,18,18,18,18,18,18,22,22,24,24,26,26,26,2,3,3,3,3};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Map<String, String> transactions, String csrfToken) {
		jteOutput.writeContent("\n<section>\n    <h2>Transactions</h2>\n    ");
		if (transactions.isEmpty()) {
			jteOutput.writeContent("\n        <p>No transactions yet.</p>\n    ");
		} else {
			jteOutput.writeContent("\n        <ul>\n            ");
			for (Map.Entry<String, String> transaction : transactions.entrySet()) {
				jteOutput.writeContent("\n                <li>\n                    <strong>#");
				jteOutput.setContext("strong", null);
				jteOutput.writeUserContent(transaction.getKey());
				jteOutput.writeContent(":</strong>\n                    ");
				jteOutput.setContext("li", null);
				jteOutput.writeUserContent(transaction.getValue());
				jteOutput.writeContent("\n                    <a href=\"/transactions/");
				jteOutput.setContext("a", "href");
				jteOutput.writeUserContent(transaction.getKey());
				jteOutput.setContext("a", null);
				jteOutput.writeContent("\">Edit</a>\n                    <form method=\"post\"\n                          action=\"/transactions/");
				jteOutput.setContext("form", "action");
				jteOutput.writeUserContent(transaction.getKey());
				jteOutput.setContext("form", null);
				jteOutput.writeContent("/delete\">\n                        <input type=\"hidden\" name=\"csrfToken\"");
				var __jte_html_attribute_0 = csrfToken;
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n                        <button type=\"submit\">Delete</button>\n                    </form>\n                </li>\n            ");
			}
			jteOutput.writeContent("\n        </ul>\n    ");
		}
		jteOutput.writeContent("\n</section>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Map<String, String> transactions = (Map<String, String>)params.get("transactions");
		String csrfToken = (String)params.get("csrfToken");
		render(jteOutput, jteHtmlInterceptor, transactions, csrfToken);
	}
}
