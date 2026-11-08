package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class JteuserAccountGenerated {
	public static final String JTE_NAME = "userAccount.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,4,4,4,4,5,5,5,6,6,6,0,1,1,1,1};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String name, String balance) {
		jteOutput.writeContent("\n<section>\n    <h2>");
		jteOutput.setContext("h2", null);
		jteOutput.writeUserContent(name);
		jteOutput.writeContent("'s account</h2>\n    <p>Balance: <strong>");
		jteOutput.setContext("strong", null);
		jteOutput.writeUserContent(balance);
		jteOutput.writeContent("</strong></p>\n</section>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String name = (String)params.get("name");
		String balance = (String)params.get("balance");
		render(jteOutput, jteHtmlInterceptor, name, balance);
	}
}
