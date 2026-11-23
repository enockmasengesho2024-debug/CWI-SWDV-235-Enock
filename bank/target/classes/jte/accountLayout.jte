@import gg.jte.Content

@param String title
@param String csrfToken
@param Content account
@param Content detail

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <link rel="stylesheet" href="/accounts.css">
</head>
<body>
    <nav aria-label="Bank navigation">
    <strong>Javalin Bank</strong>

    <form method="post" action="/logout">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <button type="submit">Log Out</button>
    </form>
</nav>

    <main>
        ${account}
        ${detail}
    </main>

    <footer>© 2026 Javalin Bank</footer>
</body>
</html>