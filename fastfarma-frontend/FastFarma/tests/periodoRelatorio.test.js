import { test } from "node:test";
import assert from "node:assert/strict";
import { obterPeriodo } from "../src/pages/relatorio/periodoRelatorio.js";

test("últimos sete dias incluem hoje e cruzam mês e ano", () => {
    assert.deepEqual(obterPeriodo("7dias", new Date(2026, 0, 3)), { inicio: "2025-12-28", fim: "2026-01-03" });
});
test("últimos trinta dias incluem hoje e ano bissexto", () => {
    assert.deepEqual(obterPeriodo("30dias", new Date(2024, 2, 1)), { inicio: "2024-02-01", fim: "2024-03-01" });
});
test("este mês usa o primeiro dia e hoje", () => {
    assert.deepEqual(obterPeriodo("mes", new Date(2026, 9, 21)), { inicio: "2026-10-01", fim: "2026-10-21" });
});
test("todo o período não envia datas e hoje é um intervalo de um dia", () => {
    assert.deepEqual(obterPeriodo("todo"), { inicio: "", fim: "" });
    assert.deepEqual(obterPeriodo("hoje", new Date(2026, 9, 21)), { inicio: "2026-10-21", fim: "2026-10-21" });
});
