import queue
import json
import time
import glob
import os

# Global variables
inv = queue.Queue()
used = queue.Queue()
found = queue.Queue()
cls = lambda: os.system('cls' if os.name in ('nt', 'dos') else 'clear')

# Prepare folders
if not os.path.exists('stories'):
    os.mkdir('stories')
if not os.path.exists('saves'):
    os.mkdir('saves')

def load():
    # Loads a story
    cls()

    if not os.path.exists('stories'):
        os.mkdir('stories')

    files = glob.glob('stories/*.json')
    if len(files) == 0:
        print('Nejsou stazene zadne pribehy k nacteni!')
        input('Zmacknete enter pro ukonceni hry...')
        exit()

    print('Jaky pribeh chcete nacist?')
    for index, file in enumerate(files):
        fileName = os.path.normpath(file).replace('/', '\\').split('\\')[1].split('.')[0]
        print(f'{index}: {fileName}')

    print('\n')

    while True:
        answer = input('Zadejte cislo pribehu: ')
        if answer.isdigit() and int(answer) in range(len(files)):
            break
        else:
            print("\033[A\033[A")
            print("\033[A\033[A")
            print('Neplatne cislo pribehu!')
    
    with open(files[int(answer)], 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if not 'id' in data or not 'story' in data or not 'inventory' in data or not 'name' in data:
        print('Neplatny soubor!')
        return load()

    print(f'Nacten pribeh "{data["name"]}", nacitani ulozenych dat...')

    if os.path.exists(f'saves/{data["id"]}.json'):
        with open(f'saves/{data["id"]}.json', 'r', encoding='utf-8') as f:
            invData = json.load(f)
        if 'roomNmb' in invData and 'textNmb' in invData and 'inv' in invData and 'used' in invData and 'found' in invData:

            inv.put(invData['inv'])
            used.put(invData['used'])
            found.put(invData['found'])

            print('Ulozene data nactena!')
            return data, invData['roomNmb'], invData['textNmb']
        else:
            print('Neplatny soubor!')
    
    inv.put([])
    used.put([])
    found.put([])

    print('Nacteni deafultnich dat dokonceno!')
    return data, 0, 0

def save(roomNmb, textNmb, reset=False):
    # Saves a story
    cls()
    print('Ulkadani hry...')
    if not os.path.exists('saves'):
        os.mkdir('saves')
    with open(f'saves/{data["id"]}.json', 'w', encoding='utf-8') as f:
        json.dump({
            'roomNmb': roomNmb,
            'textNmb': textNmb,
            'inv': inv.queue[0] if not reset else [],
            'used': used.queue[0] if not reset else [],
            'found': found.queue[0] if not reset else [],
        }, f)
    print('Hra ulozena!')
    if reset:
        print('Vsechny ulozene data byla vyresetovana!')
        exit()
    tell(roomNmb, textNmb, skip=True, message='Hra byla ulozena!')

data, roomLoad, textload = load()
rooms = data['story']
items = data['inventory']


def inventory(roomNmb, textNmb):
    # Shows inventory
    cls()
    print('Polozky v inventari:\n')

    if len(inv.queue[0]) == 0:
        print('\x1B[3mV inventari nic neni\x1B[0m')
    else:
        for itemNmb in inv.queue[0]:
            print(f'\033[4m{items[itemNmb]["name"]}\033[0m: {items[itemNmb]["text"]}')
    
    input('\nZmacknete enter pro navraceni do hry...')
    tell(roomNmb, textNmb, skip=True)

def tell(roomNmb, textNmb, skip=False, message='', rooms=rooms):
    # Main component, handles the story
    cls()
    story = rooms[roomNmb]

    print(f'\x1B[3mNachazis se v: {story["name"]}\x1B[0m')

    # Letter by letter printing
    if not skip:
        for letter in story["texts"][textNmb]["text"]:
            print(letter, end='', flush=True)
            if letter in ['.', ',', '!', '?']:
                time.sleep(0.3)
            else:
                time.sleep(0.04)
    else:
        print(story["texts"][textNmb]["text"])
    print('\n' if not skip else '')

    # -1 Save -2 Exit, -3 Inventory
    baseAnswers = [-1, -2, -3]
    answers = [-1, -2, -3]

    # Always accessible answers
    for baseAnswer in baseAnswers:
        if baseAnswer == -1:
            print(f'\x1B[3m{baseAnswer}: Ulozit\x1B[0m')
        elif baseAnswer == -2:
            print(f'\x1B[3m{baseAnswer}: Ukoncit\x1B[0m')
        elif baseAnswer == -3:
            print(f'\x1B[3m{baseAnswer}: Inventar\x1B[0m')
    
    print('')

    # Answers printing
    link = {}
    deductable = 0
    for index, answerNmb in enumerate(story["texts"][textNmb]["answers"]):
        index -= deductable
        if [roomNmb, answerNmb] in used.queue[0] and not story["answers"][answerNmb]["repeatable"]:
            deductable += 1
            continue

        if story["answers"][answerNmb]["hidden"] and [roomNmb, answerNmb] not in found.queue[0]:
            print(f'\x1B[3m*: Skryta odpoved, zkus se porozhlednout jestli neziskas napovedu!\x1B[0m')
            continue
        
        if set(story["answers"][answerNmb]["requires"]).issubset(inv.queue[0]):
            print(f'{index}: {story["answers"][answerNmb]["text"]}')
            link[index] = answerNmb
            answers.append(index)
            continue

        itemy = []
        itemyMissing = []
        for itemNmb in story["answers"][answerNmb]["requires"]:
            itemy.append(items[itemNmb]["name"])
            if itemNmb not in inv.queue[0]:
                itemyMissing.append(items[itemNmb]["name"])
        requires = "\033[4m\x1B[3m" + "\033[0m\x1B[0m a \033[4m\x1B[3m".join(itemy) + "\033[0m\x1B[0m"
        missing = "\033[4m\x1B[3m" + "\033[0m\x1B[0m, \033[4m\x1B[3m".join(itemyMissing) + "\033[0m\x1B[0m"
        print(f'\x1B[3m*: {story["answers"][answerNmb]["text"]} (Vyzaduje: \x1B[0m{requires}\x1B[3m, chybi: \x1B[0m{missing}\x1B[3m)\x1B[0m')

    print('\n' if message == '' else '')
    if message != '':
        print(f'\x1B[3m{message}\x1B[0m')
    
    # Answer input
    while True:
        answer = input('Zadejte cislo odpovedi: ')
        if answer.lstrip("-").isdigit() and int(answer) in answers:
            break
        else:
            print("\033[A\033[A")
            print("\033[A\033[A")
            print('Neplatne cislo odpovedi!')

    # Answer handling
    answerNmb = int(answer)
    if answerNmb in baseAnswers:
        if answerNmb == -1:
            save(roomNmb, textNmb)
        elif answerNmb == -2:
            exit()
        elif answerNmb == -3:
            inventory(roomNmb, textNmb)
    else:
        answer = story["answers"][link[answerNmb]]
        for item in answer["gives"]:
            if item not in inv.queue[0]:
                inv.queue[0].append(item)
        for item in answer["takes"]:
            if item in inv.queue[0]:
                inv.queue[0].remove(item)

        for item in answer["unlocks"]:
            if [item['room'], item['unlock']] not in found.queue[0]:
                found.queue[0].append([item['room'], item['unlock']])

        if not answer["repeatable"]:
            used.queue[0].append([roomNmb, answerNmb])

        if answer["goto"] == -1:
            save(0, 0, reset=True)
            exit()

        tell(answer["goto"], answer["tell"], rooms=rooms)
        

tell(roomLoad, textload)