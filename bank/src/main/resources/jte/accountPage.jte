@import edu.cwi.UserAccount
@import edu.cwi.Account
@import java.util.List

@param String title
@param UserAccount account
@param List<Account> accounts
@param Long accountId
@param String csrfToken

@template.accountLayout(
    title = title,
    csrfToken = csrfToken,
    account = @`
        @template.userAccount(name = account.name(), balance = account.formatBalance())
    `,
    detail = @`
        <p><a href="/accounts">Back to Accounts</a></p>
        @if(accounts.isEmpty())
            <p>You do not have a bank account yet.</p>
            <p>Administrators can create customers through the users API.</p>
        @else
                @for(Account item : accounts)
                    <a href="/account?accountId=${item.getId()}">${item.getAccountType()} #${item.getId()}</a>
                @endfor
            </nav>
            <h2>Account #${accountId}</h2>
            <section>
                <h2>Add transaction</h2>
                <p>Use a positive amount for a deposit and a negative amount for a withdrawal.</p>
                <form method="post" action="/transactions">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="accountId" value="${accountId}">
                    <p>
                        <label for="description">Description</label>
                        <input id="description" name="description" type="text" maxlength="255" required>
                    </p>
                    <p>
                        <label for="amount">Amount</label>
                        <input id="amount" name="amount" type="number" step="0.01" required>
                    </p>
                    <button type="submit">Save transaction</button>
                </form>
            </section>
            @template.transactionsList(transactions = account.transactions(), csrfToken = csrfToken)
        @endif
    `
)
