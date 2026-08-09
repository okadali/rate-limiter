-- KEYS[1]: Token sayısını tutan anahtar
-- KEYS[2]: Son dolum zamanını tutan anahtar
-- ARGV[1]: Kova kapasitesi (Capacity)
-- ARGV[2]: Saniye başına dolum hızı (Refill Rate)
-- ARGV[3]: Şu anki zaman (Milisaniye cinsinden Java'dan gönderilir)

local key_count = KEYS[1]
local key_last_refill = KEYS[2]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local current_time = tonumber(ARGV[3])

-- Redis'ten mevcut değerleri al, yoksa varsayılan değerleri kullan
local last_refill = tonumber(redis.(call'GET', key_last_refill) or current_time)
local tokens = tonumber(redis.call('GET', key_count) or capacity)

-- Geçen süreyi ve eklenecek token miktarını hesapla
local elapsed_ms = current_time - last_refill
local elapsed_sec = elapsed_ms / 1000.0
local tokens_to_add = math.floor(elapsed_sec * refill_rate)

-- Eğer eklenecek token varsa, kovayı doldur ve son dolum zamanını güncelle
if tokens_to_add > 0 then
    tokens = math.min(capacity, tokens + tokens_to_add)
    last_refill = current_time
end

local allowed = 0
-- Eğer kovada token varsa, bir tane tüket ve durumu güncelle
if tokens > 0 then
    tokens = tokens - 1
    allowed = 1

    -- Değerleri Redis'e geri yaz ve memory sızıntısını önlemek için TTL (Ömür) ata
    redis.call('SET', key_count, tokens, 'EX', 3600)
    redis.call('SET', key_last_refill, last_refill, 'EX', 3600)
end

return allowed