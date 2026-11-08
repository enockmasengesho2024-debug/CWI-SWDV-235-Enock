package gg.jte.generated.ondemand;
import edu.cwi.UserAccount;
@SuppressWarnings("unchecked")
public final class JteaccountPageGenerated {
	public static final String JTE_NAME = "accountPage.jte";
	public static final int[] JTE_LINE_INFO = {0,0,2,2,2,2,5,5,11,11,12,15,16,16,16,16,38,40,41,41,41,41,41,2,3,3,3,3};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String title, UserAccount account) {
		jteOutput.writeContent("\n");
		gg.jte.generated.ondemand.JteaccountLayoutGenerated.render(jteOutput, jteHtmlInterceptor, title, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n        ");
				gg.jte.generated.ondemand.JteuserAccountGenerated.render(jteOutput, jteHtmlInterceptor, account.name(), account.formatBalance());
				jteOutput.writeContent("\n    ");
			}
		}, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n        <section>\n            <h2>Add transaction</h2>\n            <p>Use a positive amount for a deposit and a negative amount for a withdrawal.</p>\n\n            <form method=\"post\" action=\"/transactions\">\n                <p>\n                    <label for=\"description\">Description</label>\n                    <input id=\"description\" name=\"description\" type=\"text\"\n                           maxlength=\"120\" required>\n                </p>\n\n                <p>\n                    <label for=\"amount\">Amount</label>\n                    <input id=\"amount\" name=\"amount\" type=\"number\"\n                           step=\"0.01\" required>\n                </p>\n\n                <button type=\"submit\">Save transaction</button>\n            </form>\n        </section>\n\n        ");
				gg.jte.generated.ondemand.JtetransactionsListGenerated.render(jteOutput, jteHtmlInterceptor, account.transactions());
				jteOutput.writeContent("\n    ");
			}
		});
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String title = (String)params.get("title");
		UserAccount account = (UserAccount)params.get("account");
		render(jteOutput, jteHtmlInterceptor, title, account);
	}
}
