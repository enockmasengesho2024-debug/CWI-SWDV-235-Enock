package gg.jte.generated.ondemand;
import java.util.Map;
@SuppressWarnings("unchecked")
public final class JtetransactionsListGenerated {
	public static final String JTE_NAME = "transactionsList.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,1,6,6,6,8,8,10,10,12,12,12,13,13,13,15,15,15,15,18,18,18,18,22,22,24,24,25,25,25,1,1,1,1};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Map<String, String> transactions) {
		jteOutput.writeContent("\n<section>\n    <h2>Transactions</h2>\n\n    ");
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
				jteOutput.writeContent("\n\n                    <a href=\"/transactions/");
				jteOutput.setContext("a", "href");
				jteOutput.writeUserContent(transaction.getKey());
				jteOutput.setContext("a", null);
				jteOutput.writeContent("\">Edit</a>\n\n                    <form method=\"post\"\n                          action=\"/transactions/");
				jteOutput.setContext("form", "action");
				jteOutput.writeUserContent(transaction.getKey());
				jteOutput.setContext("form", null);
				jteOutput.writeContent("/delete\">\n                        <button type=\"submit\">Delete</button>\n                    </form>\n                </li>\n            ");
			}
			jteOutput.writeContent("\n        </ul>\n    ");
		}
		jteOutput.writeContent("\n</section>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Map<String, String> transactions = (Map<String, String>)params.get("transactions");
		render(jteOutput, jteHtmlInterceptor, transactions);
	}
}
