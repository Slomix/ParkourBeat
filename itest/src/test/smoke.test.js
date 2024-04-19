const mineflayer = require('mineflayer')
const {Rcon} = require("rcon-client");

const host = 'localhost'
const rconPort = 25575
const rconPassword = 'rcon-test-password'
const defaultDelay = 100

let playerName
let bot
let rcon

beforeAll(async () => {
    rcon = await Rcon.connect({
        host: host,
        port: rconPort,
        password: rconPassword
    })
})

beforeEach(async () => {
    playerName = `Test${Math.floor(Math.random() * 1000000)}`
    bot = mineflayer.createBot({
        host: host,
        username: playerName,
        auth: 'offline'
    })

    await new Promise(resolve => {
        bot.on('spawn', () => resolve())
    })
})

describe('Дымовые тесты', () => {
    test('Игрок успешно создает карту', async () => {
        await executeCommand('create')
        // Я заебусь это писать...
    })
})

afterEach(async () => {
    bot.quit()
})

afterAll(async () => {
    rcon.end()
})

async function delay(ms = defaultDelay) {
    return new Promise(e => setTimeout(e, ms))
}

function executeCommand(command) {
    return new Promise((resolve, reject) => {
        function messageListener(message) {
            bot.off('messagestr', messageListener)
            resolve(message)
        }
        bot.on('messagestr', messageListener)
        bot.chat(`/${command}`)
    })
}