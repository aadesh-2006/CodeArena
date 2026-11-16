import React from 'react';

interface FormattedContentProps {
  content: string;
  className?: string;
}

/**
 * Parses inline formatting (bold, italic, inline code) into React elements
 * without injecting raw HTML.
 */
function renderInlineText(text: string): React.ReactNode[] {
  // Regex matches `inline code`, **bold**, *italic*
  const tokenRegex = /(`[^`]+`|\*\*[^*]+\*\*|\*[^*]+\*)/g;
  const parts = text.split(tokenRegex);

  return parts.map((part, index) => {
    if (part.startsWith('`') && part.endsWith('`') && part.length >= 2) {
      return (
        <code
          key={index}
          className="bg-gray-800 text-blue-300 font-mono text-xs px-1.5 py-0.5 rounded border border-gray-700/60"
        >
          {part.slice(1, -1)}
        </code>
      );
    }
    if (part.startsWith('**') && part.endsWith('**') && part.length >= 4) {
      return (
        <strong key={index} className="font-semibold text-white">
          {part.slice(2, -2)}
        </strong>
      );
    }
    if (part.startsWith('*') && part.endsWith('*') && part.length >= 2) {
      return (
        <em key={index} className="italic text-gray-300">
          {part.slice(1, -1)}
        </em>
      );
    }
    return <React.Fragment key={index}>{part}</React.Fragment>;
  });
}

/**
 * Safe block-level Markdown renderer that never invokes dangerouslySetInnerHTML.
 */
export const FormattedContent: React.FC<FormattedContentProps> = ({
  content,
  className = '',
}) => {
  if (!content || !content.trim()) {
    return null;
  }

  const lines = content.split('\n');
  const elements: React.ReactNode[] = [];

  let inCodeBlock = false;
  let codeBlockLines: string[] = [];
  let currentListItems: React.ReactNode[] = [];
  let isNumberedList = false;

  const flushList = (key: string) => {
    if (currentListItems.length > 0) {
      if (isNumberedList) {
        elements.push(
          <ol
            key={key}
            className="list-decimal list-inside space-y-1 my-2 text-gray-300 pl-2 leading-relaxed"
          >
            {currentListItems}
          </ol>
        );
      } else {
        elements.push(
          <ul
            key={key}
            className="list-disc list-inside space-y-1 my-2 text-gray-300 pl-2 leading-relaxed"
          >
            {currentListItems}
          </ul>
        );
      }
      currentListItems = [];
      isNumberedList = false;
    }
  };

  for (let i = 0; i < lines.length; i++) {
    const rawLine = lines[i];
    const trimmed = rawLine.trim();

    // Check code block fence ```
    if (trimmed.startsWith('```')) {
      if (inCodeBlock) {
        // Close code block
        elements.push(
          <pre
            key={`code-${i}`}
            className="bg-[#090d16] border border-gray-800 rounded-lg p-3 my-3 overflow-x-auto text-xs font-mono text-gray-300 leading-normal"
          >
            <code>{codeBlockLines.join('\n')}</code>
          </pre>
        );
        codeBlockLines = [];
        inCodeBlock = false;
      } else {
        flushList(`list-before-code-${i}`);
        inCodeBlock = true;
      }
      continue;
    }

    if (inCodeBlock) {
      codeBlockLines.push(rawLine);
      continue;
    }

    // Blank line
    if (!trimmed) {
      flushList(`list-before-blank-${i}`);
      continue;
    }

    // Headers
    if (trimmed.startsWith('### ')) {
      flushList(`list-before-h3-${i}`);
      elements.push(
        <h4
          key={`h3-${i}`}
          className="text-sm font-semibold text-gray-100 uppercase tracking-wider mt-4 mb-2"
        >
          {renderInlineText(trimmed.slice(4))}
        </h4>
      );
      continue;
    }

    if (trimmed.startsWith('## ')) {
      flushList(`list-before-h2-${i}`);
      elements.push(
        <h3
          key={`h2-${i}`}
          className="text-base font-bold text-white mt-5 mb-2.5"
        >
          {renderInlineText(trimmed.slice(3))}
        </h3>
      );
      continue;
    }

    if (trimmed.startsWith('# ')) {
      flushList(`list-before-h1-${i}`);
      elements.push(
        <h2
          key={`h1-${i}`}
          className="text-lg font-extrabold text-white mt-6 mb-3"
        >
          {renderInlineText(trimmed.slice(2))}
        </h2>
      );
      continue;
    }

    // Blockquote
    if (trimmed.startsWith('> ')) {
      flushList(`list-before-quote-${i}`);
      elements.push(
        <blockquote
          key={`quote-${i}`}
          className="border-l-2 border-blue-500 pl-3 my-2 text-gray-400 italic text-sm"
        >
          {renderInlineText(trimmed.slice(2))}
        </blockquote>
      );
      continue;
    }

    // Bulleted list item
    if (trimmed.startsWith('- ') || trimmed.startsWith('* ')) {
      if (isNumberedList) {
        flushList(`flush-before-bullet-${i}`);
      }
      isNumberedList = false;
      currentListItems.push(
        <li key={`li-${i}`}>{renderInlineText(trimmed.slice(2))}</li>
      );
      continue;
    }

    // Numbered list item
    const numberedMatch = trimmed.match(/^(\d+)\.\s+(.+)$/);
    if (numberedMatch) {
      if (!isNumberedList && currentListItems.length > 0) {
        flushList(`flush-before-num-${i}`);
      }
      isNumberedList = true;
      currentListItems.push(
        <li key={`num-li-${i}`}>{renderInlineText(numberedMatch[2])}</li>
      );
      continue;
    }

    // Normal paragraph line
    flushList(`list-before-p-${i}`);
    elements.push(
      <p key={`p-${i}`} className="my-2 text-gray-300 leading-relaxed text-sm">
        {renderInlineText(rawLine)}
      </p>
    );
  }

  // Flush any trailing elements
  if (inCodeBlock && codeBlockLines.length > 0) {
    elements.push(
      <pre
        key="code-trailing"
        className="bg-[#090d16] border border-gray-800 rounded-lg p-3 my-3 overflow-x-auto text-xs font-mono text-gray-300 leading-normal"
      >
        <code>{codeBlockLines.join('\n')}</code>
      </pre>
    );
  }
  flushList('list-trailing');

  return <div className={`space-y-1 ${className}`}>{elements}</div>;
};
