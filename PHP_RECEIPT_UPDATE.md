# PHP Receipt Page – ছোট আপডেট (ঐচ্ছিক)

আপনার বর্তমান `receipt_sunmi.php` **ইতিমধ্যেই কাজ করবে**।  
তবুও আরও রোবাস্ট করতে চাইলে নিচের `<script>` অংশটা ব্যবহার করতে পারেন (পুরনো কোড রিপ্লেস করে):

```html
<script>
(function () {
    function cleanText(text) {
        return (text || '')
            .replace(/\u00a0/g, ' ')
            .replace(/[ \t]+\n/g, '\n')
            .replace(/\n[ \t]+/g, '\n')
            .replace(/\n{3,}/g, '\n\n')
            .trim();
    }

    function getReceiptText() {
        var copies = document.querySelectorAll('.receipt');
        var parts = [];
        copies.forEach(function (copy, index) {
            parts.push(cleanText(copy.innerText || copy.textContent || ''));
            if (index < copies.length - 1) {
                parts.push('--------------------------------');
                parts.push('CUT HERE');
                parts.push('--------------------------------');
            }
        });
        return parts.join('\n');
    }

    function sendToPrinter(text) {
        var status = document.getElementById('printStatus');

        // 1) New bridge
        if (window.SunmiBridge && typeof window.SunmiBridge.printReceipt === 'function') {
            if (status) status.textContent = 'Sending to SUNMI printer...';
            try {
                window.SunmiBridge.printReceipt(text);
                if (status) status.textContent = 'Print command sent ✓';
                return true;
            } catch (e) {
                console.error(e);
            }
        }

        // 2) Old style bridge
        if (window.lee && typeof window.lee.funAndroid === 'function') {
            if (status) status.textContent = 'Sending to SUNMI printer...';
            try {
                window.lee.funAndroid(text);
                if (status) status.textContent = 'Print command sent ✓';
                return true;
            } catch (e) {
                console.error(e);
            }
        }

        if (status) status.textContent = 'SUNMI Print Bridge পাওয়া যায়নি';
        return false;
    }

    window.doPrint = function () {
        var text = getReceiptText();
        if (!text) {
            var status = document.getElementById('printStatus');
            if (status) status.textContent = 'Receipt text পাওয়া যায়নি';
            return;
        }
        if (!sendToPrinter(text)) {
            alert('SUNMI Print Bridge connected নেই.\nReceipt-টি BSD Sunmi Print APK-এর ভিতরে খুলুন.');
        }
    };

    // Auto print after page load (WebView-এ)
    window.addEventListener('load', function () {
        setTimeout(function () {
            window.doPrint();
        }, 900);
    });
})();
</script>
```

**মনে রাখবেন:** Chrome ব্রাউজারে এই ব্রিজ পাওয়া যাবে না। শুধুমাত্র এই WebView APK-এর ভিতরে কাজ করবে।
