package gg.jte.generated.ondemand;
import edu.cwi.Account;
import edu.cwi.Transaction;
import java.util.List;
import edu.cwi.LedgerService;
@SuppressWarnings("unchecked")
public final class JteaccountDetailGenerated {
	public static final String JTE_NAME = "accountDetail.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,9,9,14,14,19,19,19,31,31,31,32,32,32,36,36,36,36,40,40,40,40,40,40,40,40,40,41,41,41,41,41,41,41,41,41,72,72,74,74,74,75,75,75,76,76,76,78,78,79,79,83,83,87,87,87,87,87,4,5,6,7,7,7,7};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String name, Account account, List<Transaction> transactions, String csrfToken) {
		jteOutput.writeContent("\n");
		gg.jte.generated.ondemand.JteaccountLayoutGenerated.render(jteOutput, jteHtmlInterceptor, "My Account", csrfToken, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n        <div class=\"detail-toolbar\">\n            <a href=\"/accounts\">Back to Accounts</a>\n        </div>\n\n        <h2>");
				jteOutput.setContext("h2", null);
				jteOutput.writeUserContent(name);
				jteOutput.writeContent("</h2>\n        <p>User Account</p>\n\n        <table aria-label=\"Account summary\">\n            <thead>\n                <tr>\n                    <th>Account #</th>\n                    <th>Balance</th>\n                </tr>\n            </thead>\n            <tbody>\n                <tr>\n                    <td>");
				jteOutput.setContext("td", null);
				jteOutput.writeUserContent(account.getId());
				jteOutput.writeContent("</td>\n                    <td>$");
				jteOutput.setContext("td", null);
				jteOutput.writeUserContent(account.getBalance().toPlainString());
				jteOutput.writeContent("</td>\n                </tr>\n            </tbody>\n        </table>\n    ");
			}
		}, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n        <section class=\"account-transactions\">\n           \n            <form method=\"post\" action=\"/transaction\">\n                <input type=\"hidden\" name=\"accountId\"");
				var __jte_html_attribute_0 = account.getId();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n                <input type=\"hidden\" name=\"csrfToken\"");
				var __jte_html_attribute_1 = csrfToken;
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_1);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n\n                <h3>Add transaction</h3>\n\n                <p>\n                    <label for=\"description\">Description</label>\n                    <input id=\"description\" name=\"description\"\n                        type=\"text\" maxlength=\"255\" required>\n                </p>\n\n                <p>\n                    <label for=\"amount\">Amount</label>\n                    <input id=\"amount\" name=\"amount\"\n                        type=\"number\" step=\"0.01\" required>\n                </p>\n\n                <p>Use a positive amount for a deposit and a negative amount for a withdrawal.</p>\n                <button type=\"submit\">Save transaction</button>\n            </form>\n\n            <h3>Transactions</h3>\n\n            <table aria-label=\"Transactions\">\n                <thead>\n                    <tr>\n                        <th>#</th>\n                        <th>Description</th>\n                        <th>Amount</th>\n                    </tr>\n                </thead>\n                <tbody>\n                    ");
				for (Transaction transaction : transactions) {
					jteOutput.writeContent("\n                        <tr>\n                            <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(transaction.getId());
					jteOutput.writeContent("</td>\n                            <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(transaction.getDescription());
					jteOutput.writeContent("</td>\n                            <td>$");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(LedgerService.signed(transaction).toPlainString());
					jteOutput.writeContent("</td>\n                        </tr>\n                    ");
				}
				jteOutput.writeContent("\n                    ");
				if (transactions.isEmpty()) {
					jteOutput.writeContent("\n                        <tr>\n                            <td colspan=\"3\">No transactions yet.</td>\n                        </tr>\n                    ");
				}
				jteOutput.writeContent("\n                </tbody>\n            </table>\n        </section>\n    ");
			}
		});
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String name = (String)params.get("name");
		Account account = (Account)params.get("account");
		List<Transaction> transactions = (List<Transaction>)params.get("transactions");
		String csrfToken = (String)params.get("csrfToken");
		render(jteOutput, jteHtmlInterceptor, name, account, transactions, csrfToken);
	}
}
