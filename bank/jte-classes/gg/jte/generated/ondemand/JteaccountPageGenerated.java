package gg.jte.generated.ondemand;
import edu.cwi.UserAccount;
import edu.cwi.Account;
import java.util.List;
@SuppressWarnings("unchecked")
public final class JteaccountPageGenerated {
	public static final String JTE_NAME = "accountPage.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,4,4,4,4,10,10,14,14,15,15,17,17,17,17,17,17,17,17,17,20,20,20,20,21,21,24,24,26,26,27,27,27,27,27,27,27,27,27,27,28,28,30,30,30,35,35,35,35,35,35,35,35,35,36,36,36,36,36,36,36,36,36,48,48,49,49,50,50,50,51,51,51,4,5,6,7,8,8,8,8};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String title, UserAccount account, List<Account> accounts, Long accountId, String csrfToken) {
		jteOutput.writeContent("\n");
		gg.jte.generated.ondemand.JteaccountLayoutGenerated.render(jteOutput, jteHtmlInterceptor, title, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n        ");
				gg.jte.generated.ondemand.JteuserAccountGenerated.render(jteOutput, jteHtmlInterceptor, account.name(), account.formatBalance());
				jteOutput.writeContent("\n        <form method=\"post\" action=\"/logout\">\n            <input type=\"hidden\" name=\"csrfToken\"");
				var __jte_html_attribute_0 = csrfToken;
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n            <button type=\"submit\">Log Out</button>\n        </form>\n    ");
			}
		}, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n        ");
				if (accounts.isEmpty()) {
					jteOutput.writeContent("\n            <p>You do not have a bank account yet.</p>\n            <p>Administrators can create customers through the users API.</p>\n        ");
				} else {
					jteOutput.writeContent("\n            <nav aria-label=\"Your accounts\">\n                ");
					for (Account item : accounts) {
						jteOutput.writeContent("\n                    <a href=\"/account?accountId=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(item.getId());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">");
						jteOutput.setContext("a", null);
						jteOutput.writeUserContent(item.getAccountType());
						jteOutput.writeContent(" #");
						jteOutput.setContext("a", null);
						jteOutput.writeUserContent(item.getId());
						jteOutput.writeContent("</a>\n                ");
					}
					jteOutput.writeContent("\n            </nav>\n            <h2>Account #");
					jteOutput.setContext("h2", null);
					jteOutput.writeUserContent(accountId);
					jteOutput.writeContent("</h2>\n            <section>\n                <h2>Add transaction</h2>\n                <p>Use a positive amount for a deposit and a negative amount for a withdrawal.</p>\n                <form method=\"post\" action=\"/transactions\">\n                    <input type=\"hidden\" name=\"csrfToken\"");
					var __jte_html_attribute_1 = csrfToken;
					if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
						jteOutput.writeContent(" value=\"");
						jteOutput.setContext("input", "value");
						jteOutput.writeUserContent(__jte_html_attribute_1);
						jteOutput.setContext("input", null);
						jteOutput.writeContent("\"");
					}
					jteOutput.writeContent(">\n                    <input type=\"hidden\" name=\"accountId\"");
					var __jte_html_attribute_2 = accountId;
					if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
						jteOutput.writeContent(" value=\"");
						jteOutput.setContext("input", "value");
						jteOutput.writeUserContent(__jte_html_attribute_2);
						jteOutput.setContext("input", null);
						jteOutput.writeContent("\"");
					}
					jteOutput.writeContent(">\n                    <p>\n                        <label for=\"description\">Description</label>\n                        <input id=\"description\" name=\"description\" type=\"text\" maxlength=\"255\" required>\n                    </p>\n                    <p>\n                        <label for=\"amount\">Amount</label>\n                        <input id=\"amount\" name=\"amount\" type=\"number\" step=\"0.01\" required>\n                    </p>\n                    <button type=\"submit\">Save transaction</button>\n                </form>\n            </section>\n            ");
					gg.jte.generated.ondemand.JtetransactionsListGenerated.render(jteOutput, jteHtmlInterceptor, account.transactions(), csrfToken);
					jteOutput.writeContent("\n        ");
				}
				jteOutput.writeContent("\n    ");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String title = (String)params.get("title");
		UserAccount account = (UserAccount)params.get("account");
		List<Account> accounts = (List<Account>)params.get("accounts");
		Long accountId = (Long)params.get("accountId");
		String csrfToken = (String)params.get("csrfToken");
		render(jteOutput, jteHtmlInterceptor, title, account, accounts, accountId, csrfToken);
	}
}
