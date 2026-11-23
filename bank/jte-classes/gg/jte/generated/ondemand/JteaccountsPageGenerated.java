package gg.jte.generated.ondemand;
import edu.cwi.Account;
import java.util.List;
@SuppressWarnings("unchecked")
public final class JteaccountsPageGenerated {
	public static final String JTE_NAME = "accountsPage.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,3,3,3,3,7,7,12,12,13,13,13,15,15,15,15,16,16,18,18,28,28,30,30,30,31,31,31,32,32,32,32,34,34,37,37,38,38,38,38,38,3,4,5,5,5,5};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String name, List<Account> accounts, String csrfToken) {
		jteOutput.writeContent("\n");
		gg.jte.generated.ondemand.JteaccountLayoutGenerated.render(jteOutput, jteHtmlInterceptor, "My Accounts", csrfToken, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n        <h2>");
				jteOutput.setContext("h2", null);
				jteOutput.writeUserContent(name);
				jteOutput.writeContent("</h2>\n        <p>User Accounts</p>\n    ");
			}
		}, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n        ");
				if (accounts.isEmpty()) {
					jteOutput.writeContent("\n            <p>You do not have a bank account yet.</p>\n        ");
				} else {
					jteOutput.writeContent("\n            <table>\n                <thead>\n                    <tr>\n                        <th>Account</th>\n                        <th>Balance</th>\n                        <th>Details</th>\n                    </tr>\n                </thead>\n                <tbody>\n                    ");
					for (Account item : accounts) {
						jteOutput.writeContent("\n                        <tr>\n                            <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(item.getId());
						jteOutput.writeContent("</td>\n                            <td>$");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(item.getBalance().toPlainString());
						jteOutput.writeContent("</td>\n                            <td><a href=\"/account/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(item.getId());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">Detail</a></td>\n                        </tr>\n                    ");
					}
					jteOutput.writeContent("\n                </tbody>\n            </table>\n        ");
				}
				jteOutput.writeContent("\n    ");
			}
		});
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String name = (String)params.get("name");
		List<Account> accounts = (List<Account>)params.get("accounts");
		String csrfToken = (String)params.get("csrfToken");
		render(jteOutput, jteHtmlInterceptor, name, accounts, csrfToken);
	}
}
