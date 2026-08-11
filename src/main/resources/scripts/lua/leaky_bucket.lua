-- KEYS[1]: Kovadaki su seviyesini tutan anahtar
-- KEYS[2]: Suyun en son ne zaman sızdığını (hesaplandığını) tutan anahtar
-- ARGV[1]: Kovanın kapasitesi (Maksimum tutulabilecek istek/su miktarı)
-- ARGV[2]: Sızdırma hızı (Saniyede kaç istek/damla sızacak)
-- ARGV[3]: Şu anki zaman (Milisaniye)

local water_key = KEYS[1]
local time_key = KEYS[2]
local capacity = tonumber(ARGV[1])
local leak_rate_per_sec = tonumber(ARGV[2])
local current_time_ms = tonumber(ARGV[3])

-- Mevcut su seviyesini ve son sızdırma zamanını al
local water_level = tonumber(redis.call('GET', water_key) or '0')
local last_leak_time = tonumber(redis.call('GET', time_key) or current_time_ms)

-- Geçen sürede ne kadar suyun (isteğin) sızdığını hesapla
local elapsed_ms = current_time_ms - last_leak_time
local elapsed_sec = elapsed_ms / 1000.0
local leaked_amount = math.floor(elapsed_sec * leak_rate_per_sec)

-- Eğer sızan su varsa, kovanın içindeki su seviyesini düşür
if leaked_amount > 0 then
    -- Su seviyesi 0'ın altına düşemez (math.max)
    water_level = math.max(0, water_level - leaked_amount)
    last_leak_time = current_time_ms
end

-- Yeni gelen isteğin kovaya sığıp sığmadığını kontrol et
if water_level < capacity then
    water_level = water_level + 1 -- Sığıyorsa kovaya 1 damla (istek) ekle

    -- Değerleri güncelle ve TTL (ömür) ata
    redis.call('SET', water_key, water_level, 'EX', 3600)
    redis.call('SET', time_key, last_leak_time, 'EX', 3600)

    return 1 -- İzin verildi
else
    -- Kova taştı, isteği reddet. (Ancak yine de sızdırma zamanını güncelle)
    redis.call('SET', water_key, water_level, 'EX', 3600)
    redis.call('SET', time_key, last_leak_time, 'EX', 3600)

    return 0 -- Reddedildi (Rate Limit Aşıldı)
end