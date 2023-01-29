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
    fileName = file.split('\\')[1].split('.')[0]
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
    exit()

print(f'Nacten pribeh "{data["name"]}", nacitani ulozenych dat...')

success = False
if os.path.exists(f'saves/{data["id"]}.json'):
    with open(f'saves/{data["id"]}.json', 'r', encoding='utf-8') as f:
        invData = json.load(f)
    if 'roomNmb' in invData and 'textNmb' in invData and 'inv' in invData and 'used' in invData and 'found' in invData:

        inv.put(invData['inv'])
        used.put(invData['used'])
        found.put(invData['found'])

        print('Ulozene data nactena!')
        roomLoad, textload = invData['roomNmb'], invData['textNmb']
        success = True
    else:
        print('Neplatny soubor!')

if not success:
    inv.put([])
    used.put([])
    found.put([])
    roomLoad, textload = 0, 0

    print('Nacteni deafultnich dat dokonceno!')

def save(roomNmb, textNmb, reset=False):
    # Saves a story
    cls()
    print('Ulkadani hry...')
    if not os.path.exists('saves'):
        os.mkdir('saves')
    with open(f'saves/{data["id"]}.json', 'w', encoding='utf-8') as f:
        json.dump({
            'roomNmb': roomNmb if not reset else 0,
            'textNmb': textNmb if not reset else 0,
            'inv': inv.queue[0] if not reset else [],
            'used': used.queue[0] if not reset else [],
            'found': found.queue[0] if not reset else [],
        }, f)
    print('Hra ulozena!')
    if reset:
        print('Vsechny ulozene data byla vyresetovana!')
        exit()
    tell(roomNmb, textNmb, skip=True, message='Hra byla ulozena!')

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

def tell(roomNmb, textNmb, skip=False, message=''):
    # Main component, handles the story
    cls()
    room = rooms[roomNmb]

    print(f'\x1B[3mNachazis se v: {room["name"]}\x1B[0m')

    # Letter by letter printing
    if not skip:
        for letter in room["texts"][textNmb]["text"]:
            print(letter, end='', flush=True)
            if letter in ['.', ',', '!', '?', ';']:
                time.sleep(0.3)
            else:
                time.sleep(0.04)
        print('')
    else:
        print(room["texts"][textNmb]["text"])
    print('')

    # -1 Save -2 Exit, -3 Inventory
    answers = [-1, -2, -3]

    # Answers printing
    link = {}
    deductable = 0
    for index, answerNmb in enumerate(room["texts"][textNmb]["answers"]):
        index -= deductable
        answere = room["answers"][answerNmb]
        if not answere["repeatable"] and [roomNmb, answerNmb] in used.queue[0]:
            deductable += 1
            continue

        if answere["hidden"] and [roomNmb, answerNmb] not in found.queue[0]:
            deductable += 1
            continue
        
        if set(answere["requires"]).issubset(inv.queue[0]):
            print(f'{index}: {answere["text"]}')
            link[index] = answerNmb
            answers.append(index)
            continue

        itemy = []
        itemyMissing = []
        for itemNmb in answere["requires"]:
            itemy.append(items[itemNmb]["name"])
            if itemNmb not in inv.queue[0]:
                itemyMissing.append(items[itemNmb]["name"])
        requires = "\033[4m\x1B[3m" + "\033[0m\x1B[0m a \033[4m\x1B[3m".join(itemy) + "\033[0m\x1B[0m"
        missing = "\033[4m\x1B[3m" + "\033[0m\x1B[0m, \033[4m\x1B[3m".join(itemyMissing) + "\033[0m\x1B[0m"
        print(f'\x1B[3m*: {answere["text"]} (Vyzaduje: \x1B[0m{requires}\x1B[3m, chybi: \x1B[0m{missing}\x1B[3m)\x1B[0m')

    print('')
    # Always accessible answers
    print(f'\x1B[3m-1: Ulozit\x1B[0m')
    print(f'\x1B[3m-2: Ukoncit\x1B[0m')
    print(f'\x1B[3m-3: Inventar\x1B[0m')

    print('\n' if not message else '')
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
    if answerNmb == -1:
        save(roomNmb, textNmb)
    elif answerNmb == -2:
        exit()
    elif answerNmb == -3:
        inventory(roomNmb, textNmb)
    else:
        answer = room["answers"][link[answerNmb]]
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
            used.queue[0].append([roomNmb, link[answerNmb]])

        if answer["goto"] == -1:
            save(0, 0, reset=True)
            exit()

        tell(answer["goto"], answer["tell"])
        

tell(roomLoad, textload)
