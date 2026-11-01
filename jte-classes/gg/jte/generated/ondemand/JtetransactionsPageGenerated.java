package gg.jte.generated.ondemand;
import java.util.List;
import edu.cwi.TransactionsController.TransactionData;
@SuppressWarnings("unchecked")
public final class JtetransactionsPageGenerated {
	public static final String JTE_NAME = "transactionsPage.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,2,2,2,29,29,29,31,31,43,43,45,45,45,46,46,46,47,47,47,49,49,49,49,51,51,51,51,56,56,60,60,60,2,2,2,2};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, List<TransactionData> transactions) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>Transactions</title>\n</head>\n<body>\n    <h1>Transactions</h1>\n\n    <h2>Add transaction</h2>\n    <form method=\"post\" action=\"/transactions\">\n        <p>\n            <label for=\"description\">Description</label><br>\n            <input id=\"description\" name=\"description\" type=\"text\"\n                   maxlength=\"120\" required>\n        </p>\n        <p>\n            <label for=\"amount\">Amount</label><br>\n            <input id=\"amount\" name=\"amount\" type=\"number\"\n                   step=\"0.01\" required>\n        </p>\n        <button type=\"submit\">Save transaction</button>\n    </form>\n\n    <h2>Saved transactions</h2>\n    ");
		if (transactions.isEmpty()) {
			jteOutput.writeContent("\n        <p>No transactions yet.</p>\n    ");
		}
		jteOutput.writeContent("\n\n    <table>\n        <thead>\n            <tr>\n                <th>ID</th>\n                <th>Description</th>\n                <th>Amount</th>\n                <th>Actions</th>\n            </tr>\n        </thead>\n        <tbody>\n            ");
		for (TransactionData transaction : transactions) {
			jteOutput.writeContent("\n                <tr>\n                    <td>");
			jteOutput.setContext("td", null);
			jteOutput.writeUserContent(transaction.id());
			jteOutput.writeContent("</td>\n                    <td>");
			jteOutput.setContext("td", null);
			jteOutput.writeUserContent(transaction.description());
			jteOutput.writeContent("</td>\n                    <td>");
			jteOutput.setContext("td", null);
			jteOutput.writeUserContent(transaction.amount().toPlainString());
			jteOutput.writeContent("</td>\n                    <td>\n                        <a href=\"/transactions/");
			jteOutput.setContext("a", "href");
			jteOutput.writeUserContent(transaction.id());
			jteOutput.setContext("a", null);
			jteOutput.writeContent("\">View / edit</a>\n                        <form method=\"post\"\n                              action=\"/transactions/");
			jteOutput.setContext("form", "action");
			jteOutput.writeUserContent(transaction.id());
			jteOutput.setContext("form", null);
			jteOutput.writeContent("/delete\">\n                            <button type=\"submit\">Delete</button>\n                        </form>\n                    </td>\n                </tr>\n            ");
		}
		jteOutput.writeContent("\n        </tbody>\n    </table>\n</body>\n</html>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		List<TransactionData> transactions = (List<TransactionData>)params.get("transactions");
		render(jteOutput, jteHtmlInterceptor, transactions);
	}
}
