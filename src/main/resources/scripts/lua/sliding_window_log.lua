-- KEYS[1]: İstek atan IP'ye ait anahtar (Örn: rate_limit:sliding_log:192.168.1.1)
-- ARGV[1]: Limit (Kapasite)
-- ARGV[2]: Pencere süresi (Milisaniye cinsinden. Örn: 1 dakika için 60000)
-- ARGV[3]: Şu anki zaman (Milisaniye - Score olarak kullanılacak)
-- ARGV[4]: Benzersiz İstek ID'si (UUID - ZSet'te value çakışmasını önlemek için)

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window_size_ms = tonumber(ARGV[2])
local current_time_ms = tonumber(ARGV[3])
local unique_id = ARGV[4]

-- 1. ADIM: Güvenlik sınırı (Pencerenin başlangıç zamanını bul)
local window_start_ms = current_time_ms - window_size_ms

-- 2. ADIM: Eski kayıtları temizle (ZREMRANGEBYSCORE)
-- Bu komut, Score'u (zamanı) window_start_ms'den küçük olan tüm kayıtları siler.
redis.call('ZREMRANGEBYSCORE', key, '-inf', window_start_ms)

-- 3. ADIM: Kalan geçerli istek sayısını al
local current_count = redis.call('ZCARD', key)

-- 4. ADIM: Limit kontrolü
if current_count >= limit then
    return 0 -- Limit aşıldı, isteği reddet
end

-- 5. ADIM: Limiti aşmadıysa yeni isteği ZSet'e ekle (Score = Zaman, Value = UUID)
redis.call('ZADD', key, current_time_ms, unique_id)

-- 6. ADIM: Memory Leak Önlemi
-- Anahtarın tümüne, pencere süresi kadar ömür veriyoruz.
-- Eğer adam 1 dakika boyunca hiç istek atmazsa ZSet komple silinsin, bellekte yer kaplamasın.
redis.call('PEXPIRE', key, window_size_ms)

return 1 -- İstek kabul edildi