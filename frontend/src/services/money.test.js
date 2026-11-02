import test from 'node:test';
import assert from 'node:assert/strict';
import { parseMoney, formatMoney, todayLocal } from './money.js';

test('normaliza reais sem ponto flutuante', () => {
  assert.equal(parseMoney('45,90'), '45.90');
  assert.equal(parseMoney(' 001.2 '), '1.20');
  assert.equal(parseMoney('1'), '1.00');
  assert.equal(parseMoney('0,01'), '0.01');
  assert.equal(parseMoney('9999999999,99'), '9999999999.99');
});
test('rejeita zero, negativos, milhar, excesso de casas e estouro', () => {
  for (const value of [
    '0',
    '0,00',
    '-1',
    '1.000,00',
    '1.000',
    '2,001',
    '10000000000',
    '',
    '1e2',
    'NaN',
    'Infinity',
  ]) {
    assert.throws(() => parseMoney(value), Error, value);
  }
});
test('formata reais preservando os centavos', () => {
  assert.equal(formatMoney('9999999999.99'), 'R$ 9.999.999.999,99');
  assert.equal(formatMoney('0.01'), 'R$ 0,01');
  assert.equal(formatMoney('300'), 'R$ 300,00');
  assert.equal(formatMoney('300.5'), 'R$ 300,50');
  assert.equal(formatMoney('300.00'), 'R$ 300,00');
});
test('a data padrão usa o calendário local, inclusive à noite', () => {
  assert.equal(todayLocal(new Date(2026, 9, 12, 23, 59)), '2026-10-12');
});
