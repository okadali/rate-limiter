-- KEYS[1]: İstek atan IP veya Client'a ait anahtar
-- ARGV[1]: Limitin kendisi (Kapasite)
-- ARGV[2]: Pencere süresi (Saniye cinsinden)

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

-- INCR komutu atomiktir. Key yoksa 0 kabul eder, 1 yapar ve döner.
local current_count = redis.call('INCR', key)

-- Eğer değer 1 ise, bu pencerenin ilk isteğidir. Hemen ömrünü (TTL) belirliyoruz.
if current_count == 1 then
    redis.call('EXPIRE', key, window)
end

-- Limit kontrolü
if current_count > limit then
    return 0 -- Limit aşıldı (Red)
end

return 1 -- İzin verildi (Onay)