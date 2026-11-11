@import edu.cwi.UserAccount

@param String title
@param UserAccount account

@template.accountLayout(
    title = title,

    account = @`
        @template.userAccount(
            name = account.name(),
            balance = account.formatBalance())`,

    detail = @`
        <section>
            <h2>Add transaction</h2>
            <p>Use a positive amount for a deposit and a negative amount for a withdrawal.</p>

            <form method="post" action="/transactions">
                <p>
                    <label for="description">Description</label>
                    <input id="description" name="description" type="text"
                           maxlength="120" required>
                </p>

                <p>
                    <label for="amount">Amount</label>
                    <input id="amount" name="amount" type="number"
                           step="0.01" required>
                </p>

                <button type="submit">Save transaction</button>
            </form>
        </section>

        @template.transactionsList(
            transactions = account.transactions()
        )`
)