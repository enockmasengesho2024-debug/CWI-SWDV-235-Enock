@import java.util.Map
@param Map<String, String> transactions

<section>
    <h2>Transactions</h2>

    @if(transactions.isEmpty())
        <p>No transactions yet.</p>
    @else
        <ul>
            @for(Map.Entry<String, String> transaction : transactions.entrySet())
                <li>
                    <strong>#${transaction.getKey()}:</strong>
                    ${transaction.getValue()}

                    <a href="/transactions/${transaction.getKey()}">Edit</a>

                    <form method="post"
                          action="/transactions/${transaction.getKey()}/delete">
                        <button type="submit">Delete</button>
                    </form>
                </li>
            @endfor
        </ul>
    @endif
</section>