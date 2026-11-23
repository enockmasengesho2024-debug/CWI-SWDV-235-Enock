package gg.jte.generated.ondemand;
import edu.cwi.Account;
import edu.cwi.Transaction;
import java.util.List;
import edu.cwi.LedgerService;
@SuppressWarnings("unchecked")
public final class JteaccountDetailGenerated {
	public static final String JTE_NAME = "accountDetail.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,9,9,14,14,19,19,19,31,31,31,32,32,32,36,36,36,36,49,49,51,51,51,52,52,52,53,53,53,55,55,56,56,60,60,64,64,64,64,64,4,5,6,7,7,7,7};
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
				jteOutput.writeContent("\n        <section class=\"account-transactions\">\n            <h3>Transactions</h3>\n\n            <table aria-label=\"Transactions\">\n                <thead>\n                    <tr>\n                        <th>#</th>\n                        <th>Description</th>\n                        <th>Amount</th>\n                    </tr>\n                </thead>\n                <tbody>\n                    ");
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
