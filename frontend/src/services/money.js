// Não usa parseFloat/Number: o valor viaja até BigDecimal como string decimal.
export function parseMoney(input) {
  const text = input.trim();
  if (!/^\d{1,10}([,.]\d{1,2})?$/.test(text)) {
    throw new Error(
      'Informe um valor com até 10 dígitos e 2 casas decimais, sem separador de milhar. Ex.: 45,90.',
    );
  }
  const [whole, fraction = ''] = text.replace(',', '.').split('.');
  const integer = whole.replace(/^0+(?=\d)/, '');
  const cents = fraction.padEnd(2, '0');
  if (/^0+$/.test(integer + cents))
    throw new Error('O valor deve ser maior que zero.');
  return `${integer}.${cents}`;
}

export function formatMoney(decimal) {
  const [whole, cents = ''] = decimal.split('.');
  return `R$ ${whole.replace(/\B(?=(\d{3})+(?!\d))/g, '.')},${cents.padEnd(2, '0')}`;
}

export function todayLocal(date = new Date()) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
