@import edu.cwi.Account
@import edu.cwi.Transaction
@import java.util.List
@import edu.cwi.LedgerService
@param String name
@param Account account
@param List<Transaction> transactions
@param String csrfToken

@template.accountLayout(
    title = "My Account",
    csrfToken = csrfToken,
    account = @`
        <div class="detail-toolbar">
            <a href="/accounts">Back to Accounts</a>
        </div>

        <h2>${name}</h2>
        <p>User Account</p>

        <table aria-label="Account summary">
            <thead>
                <tr>
                    <th>Account #</th>
                    <th>Balance</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>${account.getId()}</td>
                    <td>$${account.getBalance().toPlainString()}</td>
                </tr>
            </tbody>
        </table>
    `,
    detail = @`
        <section class="account-transactions">
            <h3>Transactions</h3>

            <table aria-label="Transactions">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Description</th>
                        <th>Amount</th>
                    </tr>
                </thead>
                <tbody>
                    @for(Transaction transaction : transactions)
                        <tr>
                            <td>${transaction.getId()}</td>
                            <td>${transaction.getDescription()}</td>
                            <td>$${LedgerService.signed(transaction).toPlainString()}</td>
                        </tr>
                    @endfor
                    @if(transactions.isEmpty())
                        <tr>
                            <td colspan="3">No transactions yet.</td>
                        </tr>
                    @endif
                </tbody>
            </table>
        </section>
    `
)